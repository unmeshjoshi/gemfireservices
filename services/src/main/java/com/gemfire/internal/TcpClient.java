package com.gemfire.internal;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.UnsupportedVersionException;
import org.apache.geode.distributed.internal.tcpserver.LocatorCancelException;
import org.apache.geode.distributed.internal.tcpserver.TcpServer;
import org.apache.geode.distributed.internal.tcpserver.VersionRequest;
import org.apache.geode.distributed.internal.tcpserver.VersionResponse;
import org.apache.geode.internal.Version;
import org.apache.geode.internal.VersionedDataInputStream;
import org.apache.geode.internal.VersionedDataOutputStream;
import org.apache.geode.internal.logging.LogService;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Client for the TcpServer component of the Locator.
 * </p>
 *
 * @since GemFire 5.7
 */
public class TcpClient {

    private static final Logger logger = LogService.getLogger();

    private static final int DEFAULT_REQUEST_TIMEOUT = 60 * 2 * 1000;

    private static Map<InetSocketAddress, Short> serverVersions =
            new HashMap<InetSocketAddress, Short>();

    private final SocketCreator socketCreator;


    /**
     * Constructs a new TcpClient
     *
     * @param socketCreator the SocketCreator to use in communicating with the Locator
     */
    public TcpClient(SocketCreator socketCreator) {
        this.socketCreator = socketCreator;
    }



    /**
     * Send a request to a Locator and expect a reply
     *
     * @param addr The locator's address
     * @param port The locator's tcp/ip port
     * @param request The request message
     * @param timeout Timeout for sending the message and receiving a reply
     *
     * @return the reply
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object requestToServer(InetAddress addr, int port, Object request, int timeout)
            throws IOException, ClassNotFoundException {
        return requestToServer(addr, port, request, timeout, true);
    }

    /**
     * Send a request to a Locator
     *
     * @param addr The locator's address
     * @param port The locator's tcp/ip port
     * @param request The request message
     * @param timeout Timeout for sending the message and receiving a reply
     * @param replyExpected Whether to wait for a reply
     *
     * @return The reply, or null if no reply is expected
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object requestToServer(InetAddress addr, int port, Object request, int timeout,
                                  boolean replyExpected) throws IOException, ClassNotFoundException {
        InetSocketAddress ipAddr;
        if (addr == null) {
            ipAddr = new InetSocketAddress(port);
        } else {
            ipAddr = new InetSocketAddress(addr, port); // fix for bug 30810
        }

        long giveupTime = System.currentTimeMillis() + timeout;

        // Get the GemFire version of the TcpServer first, before sending any other request.
        short serverVersion = getServerVersion(ipAddr, timeout).shortValue();

        if (serverVersion > Version.CURRENT_ORDINAL) {
            serverVersion = Version.CURRENT_ORDINAL;
        }

        // establish the old GossipVersion for the server
        int gossipVersion = TcpServer.getCurrentGossipVersion();

        if (Version.GFE_71.compareTo(serverVersion) > 0) {
            gossipVersion = TcpServer.getOldGossipVersion();
        }

        long newTimeout = giveupTime - System.currentTimeMillis();
        if (newTimeout <= 0) {
            return null;
        }

        logger.debug("TcpClient sending {} to {}", request, ipAddr);

        Socket sock =
                socketCreator.connect(ipAddr.getAddress(), ipAddr.getPort(), (int) newTimeout, null, false);
        sock.setSoTimeout((int) newTimeout);
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(sock.getOutputStream());

            if (serverVersion < Version.CURRENT_ORDINAL) {
                out = new VersionedDataOutputStream(out, Version.fromOrdinalNoThrow(serverVersion, false));
            }

            out.writeInt(gossipVersion);
            if (gossipVersion > TcpServer.getOldGossipVersion()) {
                out.writeShort(serverVersion);
            }
            DataSerializer.writeObject(request, out);
            out.flush();

            if (replyExpected) {
                DataInputStream in = new DataInputStream(sock.getInputStream());
                in = new VersionedDataInputStream(in, Version.fromOrdinal(serverVersion, false));
                try {
                    Object response = DataSerializer.readObject(in);
                    logger.debug("received response: {}", response);
                    return response;
                } catch (EOFException ex) {
                    EOFException eof = new EOFException("Locator at " + ipAddr
                            + " did not respond. This is normal if the locator was shutdown. If it wasn't check its log for exceptions.");
                    eof.initCause(ex);
                    throw eof;
                }
            } else {
                return null;
            }
        } catch (UnsupportedVersionException ex) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Remote TcpServer version: " + serverVersion + " is higher than local version: "
                                + Version.CURRENT_ORDINAL + ". This is never expected as remoteVersion");
            }
            return null;
        } finally {
            try {
                if (replyExpected) {
                    // Since we've read a response we know that the Locator is finished
                    // with the socket and is closing it. Aborting the connection by
                    // setting SO_LINGER to zero will clean up the TIME_WAIT socket on
                    // the locator's machine.
                    if (!sock.isClosed()) {
                        sock.setSoLinger(true, 0);
                    }
                }
                sock.close();
            } catch (Exception e) {
                logger.error("Error closing socket ", e);
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private Short getServerVersion(InetSocketAddress ipAddr, int timeout)
            throws IOException, ClassNotFoundException {

        int gossipVersion = TcpServer.getCurrentGossipVersion();
        Short serverVersion = null;

        // Get GemFire version of TcpServer first, before sending any other request.
        synchronized (serverVersions) {
            serverVersion = serverVersions.get(ipAddr);
        }
        if (serverVersion != null) {
            return serverVersion;
        }

        gossipVersion = TcpServer.getOldGossipVersion();

        Socket sock = null;
        try {
            sock = socketCreator.connect(ipAddr.getAddress(), ipAddr.getPort(), timeout, null, false);
            sock.setSoTimeout(timeout);
        } catch (SSLException e) {
            throw new LocatorCancelException("Unable to form SSL connection", e);
        }

        try {
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            out = new VersionedDataOutputStream(out, Version.GFE_57);

            out.writeInt(gossipVersion);

            VersionRequest verRequest = new VersionRequest();
            DataSerializer.writeObject(verRequest, out);
            out.flush();

            InputStream inputStream = sock.getInputStream();
            DataInputStream in = new DataInputStream(inputStream);
            in = new VersionedDataInputStream(in, Version.GFE_57);
            try {
                Object readObject = DataSerializer.readObject(in);
                if (!(readObject instanceof VersionResponse)) {
                    throw new LocatorCancelException(
                            "Unrecognisable response received: object is null. This could be the result of trying to connect a non-SSL-enabled locator to an SSL-enabled locator.");
                }
                VersionResponse response = (VersionResponse) readObject;
                if (response != null) {
                    serverVersion = Short.valueOf(response.getVersionOrdinal());
                    synchronized (serverVersions) {
                        serverVersions.put(ipAddr, serverVersion);
                    }
                    return serverVersion;
                }
            } catch (EOFException ex) {
                // old locators will not recognize the version request and will close the connection
            }
        } finally {
            try {
                sock.setSoLinger(true, 0); // initiate an abort on close to shut down the server's socket
                sock.close();
            } catch (Exception e) {
                logger.error("Error closing socket ", e);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Locator " + ipAddr
                    + " did not respond to a request for its version.  I will assume it is using v5.7 for safety.");
        }
        synchronized (serverVersions) {
            serverVersions.put(ipAddr, Version.GFE_57.ordinal());
        }
        return Short.valueOf(Version.GFE_57.ordinal());
    }
}
