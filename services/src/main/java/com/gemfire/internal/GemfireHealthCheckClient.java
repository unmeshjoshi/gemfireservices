package com.gemfire.internal;

import org.apache.geode.cache.client.internal.InternalPool;
import org.apache.geode.cache.client.internal.LocatorDiscoveryCallback;
import org.apache.geode.cache.client.internal.LocatorDiscoveryCallbackAdapter;
import org.apache.geode.cache.client.internal.PoolImpl;
import org.apache.geode.cache.client.internal.locator.GetAllServersRequest;
import org.apache.geode.cache.client.internal.locator.GetAllServersResponse;
import org.apache.geode.cache.client.internal.locator.LocatorListRequest;
import org.apache.geode.cache.client.internal.locator.LocatorListResponse;
import org.apache.geode.cache.client.internal.locator.ServerLocationRequest;
import org.apache.geode.cache.client.internal.locator.ServerLocationResponse;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.distributed.internal.ServerLocation;
import org.apache.geode.internal.i18n.LocalizedStrings;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.internal.logging.log4j.LocalizedMessage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GemfireHealthCheckClient {

    private static final Logger logger = LogService.getLogger();

    private TcpClient tcpClient;

    protected static final LocatorListRequest LOCATOR_LIST_REQUEST = new LocatorListRequest();
    private static final Comparator<InetSocketAddress> SOCKET_ADDRESS_COMPARATOR =
            new Comparator<InetSocketAddress>() {
                public int compare(InetSocketAddress o1, InetSocketAddress o2) {
                    // shouldn't happen, but if it does we'll say they're the same.
                    if (o1.getAddress() == null || o2.getAddress() == null) {
                        return 0;
                    }

                    int result = o1.getAddress().getCanonicalHostName()
                            .compareTo(o2.getAddress().getCanonicalHostName());
                    if (result != 0) {
                        return result;
                    } else
                        return o1.getPort() - o2.getPort();
                }
            };
    protected final List<InetSocketAddress> initialLocators;
    private final String serverGroup;
    private AtomicReference<LocatorList> locators = new AtomicReference<LocatorList>();
    private AtomicReference<LocatorList> onlineLocators = new AtomicReference<LocatorList>();
    protected InternalPool pool;
    private final int connectionTimeout;
    private long locatorUpdateInterval;
    private volatile LocatorDiscoveryCallback locatorCallback = new LocatorDiscoveryCallbackAdapter();
    private volatile boolean isBalanced = true;
    /**
     * key is the InetSocketAddress of the locator. value will be an exception if we have already
     * found the locator to be dead. value will be null if we last saw him alive.
     */
    private final Map<InetSocketAddress, Exception> locatorState =
            new HashMap<InetSocketAddress, Exception>();

    /**
     * @param contacts
     * @param serverGroup
     * @param handshakeTimeout
     */
    public GemfireHealthCheckClient(List<InetSocketAddress> contacts, String serverGroup,
                                    int handshakeTimeout) {
        ArrayList<InetSocketAddress> tmpContacts = new ArrayList<InetSocketAddress>(contacts);
        this.locators.set(new LocatorList(tmpContacts));
        this.onlineLocators.set(new LocatorList(Collections.emptyList()));
        this.initialLocators = Collections.unmodifiableList(tmpContacts);
        this.connectionTimeout = handshakeTimeout;
        this.serverGroup = serverGroup;
        this.tcpClient = new TcpClient(new SocketCreator());
    }

    public boolean isBalanced() {
        return isBalanced;
    }

    public void initializeFromLocators() {
        LocatorListResponse response = (LocatorListResponse) queryLocators(LOCATOR_LIST_REQUEST);
        updateLocatorList(response);
    }


    public List<ServerLocation> getAllServers() {
        if (PoolImpl.TEST_DURABLE_IS_NET_DOWN) {
            return null;
        }
        GetAllServersRequest request = new GetAllServersRequest(serverGroup);
        GetAllServersResponse response = (GetAllServersResponse) queryLocators(request);
        if (response != null) {
            return response.getServers();
        } else {
            return null;
        }
    }


    public List<InetSocketAddress> getOnlineLocators() {
        if (PoolImpl.TEST_DURABLE_IS_NET_DOWN) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(onlineLocators.get().getLocators()));
    }


    private ServerLocationResponse queryOneLocator(InetSocketAddress locator,
                                                   ServerLocationRequest request) {
        InetAddress addr = locator.getAddress();
        int port = locator.getPort();
        Object returnObj = null;
        try {
            returnObj = tcpClient.requestToServer(addr, port, request, connectionTimeout);
            ServerLocationResponse response = (ServerLocationResponse) returnObj;
            if (response != null) {
                reportLiveLocator(locator);
            }
            return response;
        } catch (IOException ioe) {
            reportDeadLocator(locator, ioe);
            return null;
        } catch (ClassNotFoundException e) {
            logger.warn(
                    LocalizedMessage.create(
                            LocalizedStrings.AutoConnectionSourceImpl_RECEIVED_EXCEPTION_FROM_LOCATOR_0, locator),
                    e);
            return null;
        } catch (ClassCastException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received odd response object from the locator: {}", returnObj);
            }
            reportDeadLocator(locator, e);
            return null;
        }
    }

    protected ServerLocationResponse queryLocators(ServerLocationRequest request) {
        Iterator controllerItr = locators.get().iterator();
        ServerLocationResponse response = null;

        final boolean isDebugEnabled = logger.isDebugEnabled();
        do {
            InetSocketAddress locator = (InetSocketAddress) controllerItr.next();
            if (isDebugEnabled) {
                logger.debug("Sending query to locator {}: {}", locator, request);
            }
            response = queryOneLocator(locator, request);
            if (isDebugEnabled) {
                logger.debug("Received query response from locator {}: {}", locator, response);
            }
        } while (controllerItr.hasNext() && (response == null || !response.hasResult()));

        if (response == null) {
            return null;
        }

        return response;
    }

    protected void updateLocatorList(LocatorListResponse response) {
        if (response == null)
            return;
        isBalanced = response.isBalanced();
        List<ServerLocation> locatorResponse = response.getLocators();

        List<InetSocketAddress> newLocators = new ArrayList<InetSocketAddress>(locatorResponse.size());
        List<InetSocketAddress> newOnlineLocators =
                new ArrayList<InetSocketAddress>(locatorResponse.size());

        Set<InetSocketAddress> badLocators = new HashSet<InetSocketAddress>(initialLocators);
        for (Iterator<ServerLocation> itr = locatorResponse.iterator(); itr.hasNext(); ) {
            ServerLocation locator = itr.next();
            InetSocketAddress address = new InetSocketAddress(locator.getHostName(), locator.getPort());
            newLocators.add(address);
            newOnlineLocators.add(address);
            badLocators.remove(address);
        }

        newLocators.addAll(badLocators);

        if (logger.isInfoEnabled()) {
            LocatorList oldLocators = (LocatorList) locators.get();
            ArrayList<InetSocketAddress> removedLocators =
                    new ArrayList<InetSocketAddress>(oldLocators.getLocators());
            removedLocators.removeAll(newLocators);

            ArrayList<InetSocketAddress> addedLocators = new ArrayList<InetSocketAddress>(newLocators);
            addedLocators.removeAll(oldLocators.getLocators());
            if (!addedLocators.isEmpty()) {
                locatorCallback.locatorsDiscovered(Collections.unmodifiableList(addedLocators));
                logger.info(LocalizedMessage.create(
                        LocalizedStrings.AutoConnectionSourceImpl_AUTOCONNECTIONSOURCE_DISCOVERED_NEW_LOCATORS_0,
                        addedLocators));
            }
            if (!removedLocators.isEmpty()) {
                locatorCallback.locatorsRemoved(Collections.unmodifiableList(removedLocators));
                logger.info(LocalizedMessage.create(
                        LocalizedStrings.AutoConnectionSourceImpl_AUTOCONNECTIONSOURCE_DROPPING_PREVIOUSLY_DISCOVERED_LOCATORS_0,
                        removedLocators));
            }
        }
        LocatorList newLocatorList = new LocatorList(newLocators);
        locators.set(newLocatorList);
        onlineLocators.set(new LocatorList(newOnlineLocators));
    }

    public void start(InternalPool pool) {
        this.pool = pool;
        pool.getStats().setInitialContacts(((LocatorList) locators.get()).size());
        this.locatorUpdateInterval = Long.getLong(
                DistributionConfig.GEMFIRE_PREFIX + "LOCATOR_UPDATE_INTERVAL", pool.getPingInterval());

        if (locatorUpdateInterval > 0) {
            pool.getBackgroundProcessor().scheduleWithFixedDelay(new UpdateLocatorListTask(), 0,
                    locatorUpdateInterval, TimeUnit.MILLISECONDS);
            logger.info(LocalizedMessage.create(
                    LocalizedStrings.AutoConnectionSourceImpl_UPDATE_LOCATOR_LIST_TASK_STARTED_WITH_INTERVAL_0,
                    new Object[]{this.locatorUpdateInterval}));
        }
    }

    public void stop() {

    }

    public void setLocatorDiscoveryCallback(LocatorDiscoveryCallback callback) {
        this.locatorCallback = callback;
    }

    private synchronized void reportLiveLocator(InetSocketAddress l) {
        Object prevState = this.locatorState.put(l, null);
        if (prevState != null) {
            logger.info(LocalizedMessage.create(
                    LocalizedStrings.AutoConnectionSourceImpl_COMMUNICATION_HAS_BEEN_RESTORED_WITH_LOCATOR_0,
                    l));
        }
    }

    private synchronized void reportDeadLocator(InetSocketAddress l, Exception ex) {
        Object prevState = this.locatorState.put(l, ex);
        if (prevState == null) {
            if (ex instanceof ConnectException) {
                logger.info(LocalizedMessage
                        .create(LocalizedStrings.AutoConnectionSourceImpl_LOCATOR_0_IS_NOT_RUNNING, l), ex);
            } else {
                logger.info(LocalizedMessage.create(
                        LocalizedStrings.AutoConnectionSourceImpl_COMMUNICATION_WITH_LOCATOR_0_FAILED_WITH_1,
                        new Object[]{l, ex}), ex);
            }
        }
    }

    long getLocatorUpdateInterval() {
        return this.locatorUpdateInterval;
    }

    /**
     * A list of locators, which remembers the last known good locator.
     */
    private static class LocatorList {
        protected final List<InetSocketAddress> locators;
        protected AtomicInteger currentLocatorIndex = new AtomicInteger();

        public LocatorList(List<InetSocketAddress> locators) {
            Collections.sort(locators, SOCKET_ADDRESS_COMPARATOR);
            this.locators = Collections.unmodifiableList(locators);
        }

        public Collection<InetSocketAddress> getLocators() {
            return locators;
        }

        public int size() {
            return locators.size();
        }

        public Iterator<InetSocketAddress> iterator() {
            return new LocatorList.LocatorIterator();
        }

        @Override
        public String toString() {
            return locators.toString();
        }


        /**
         * An iterator which iterates all of the controllers, starting at the last known good
         * controller.
         */
        protected class LocatorIterator implements Iterator<InetSocketAddress> {
            private int startLocator = currentLocatorIndex.get();
            private int locatorNum = 0;

            public boolean hasNext() {
                return locatorNum < locators.size();
            }

            public InetSocketAddress next() {
                if (!hasNext()) {
                    return null;
                } else {
                    int index = (locatorNum + startLocator) % locators.size();
                    InetSocketAddress nextLocator = locators.get(index);
                    currentLocatorIndex.set(index);
                    locatorNum++;
                    return nextLocator;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    protected class UpdateLocatorListTask extends PoolImpl.PoolTask {
        @Override
        public void run2() {
            if (pool.getCancelCriterion().isCancelInProgress()) {
                return;
            }
            LocatorListResponse response = (LocatorListResponse) queryLocators(LOCATOR_LIST_REQUEST);
            updateLocatorList(response);
        }
    }
}


