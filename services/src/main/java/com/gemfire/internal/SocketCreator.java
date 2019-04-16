package com.gemfire.internal;

import org.apache.geode.distributed.ClientSocketFactory;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.ConnectionWatcher;
import org.apache.geode.internal.admin.SSLConfig;
import org.apache.geode.internal.logging.LogService;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SocketCreator {

    private static final Logger logger = LogService.getLogger();

    /**
     * Optional system property to enable GemFire usage of link-local addresses
     */
    public static final String USE_LINK_LOCAL_ADDRESSES_PROPERTY =
            DistributionConfig.GEMFIRE_PREFIX + "net.useLinkLocalAddresses";

    /**
     * True if GemFire should use link-local addresses
     */
    private static final boolean useLinkLocalAddresses =
            Boolean.getBoolean(USE_LINK_LOCAL_ADDRESSES_PROPERTY);


    private static final Map<InetAddress, String> hostNames = new HashMap<>();

    /**
     * flag to force always using DNS (regardless of the fact that these lookups can hang)
     */
    public static final boolean FORCE_DNS_USE =
            Boolean.getBoolean(DistributionConfig.GEMFIRE_PREFIX + "forceDnsUse");

    /**
     * set this to false to inhibit host name lookup
     */
    public static volatile boolean resolve_dns = true;

    /**
     * set this to false to use an inet_addr in a client's ID
     */
    public static volatile boolean use_client_host_name = true;

    /**
     * True if this SocketCreator has been initialized and is ready to use
     */
    private boolean ready = false;

    /**
     * Only print this SocketCreator's config once
     */
    private boolean configShown = false;

    /**
     * context for SSL socket factories
     */
    private SSLContext sslContext;

    private SSLConfig sslConfig;

    /**
     * A factory used to create client <code>Sockets</code>.
     */
    private ClientSocketFactory clientSocketFactory;

    /**
     * Whether to enable TCP keep alive for sockets. This boolean is controlled by the
     * gemfire.setTcpKeepAlive java system property. If not set then GemFire will enable keep-alive on
     * server->client and p2p connections.
     */
    public static final boolean ENABLE_TCP_KEEP_ALIVE;

    static {
        // bug #49484 - customers want tcp/ip keep-alive turned on by default
        // to avoid dropped connections. It can be turned off by setting this
        // property to false
        String str = System.getProperty(DistributionConfig.GEMFIRE_PREFIX + "setTcpKeepAlive");
        if (str != null) {
            ENABLE_TCP_KEEP_ALIVE = Boolean.valueOf(str);
        } else {
            ENABLE_TCP_KEEP_ALIVE = true;
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Return a client socket. This method is used by peers.
     */
    public Socket connectForServer(InetAddress inetadd, int port) throws IOException {
        return connect(inetadd, port, 0, null, false, -1);
    }

    /**
     * Return a client socket. This method is used by peers.
     */
    public Socket connectForServer(InetAddress inetadd, int port, int socketBufferSize)
            throws IOException {
        return connect(inetadd, port, 0, null, false, socketBufferSize);
    }

    /**
     * Return a client socket, timing out if unable to connect and timeout > 0 (millis). The parameter
     * <i>timeout</i> is ignored if SSL is being used, as there is no timeout argument in the ssl
     * socket factory
     */
    public Socket connect(InetAddress inetadd, int port, int timeout,
                          ConnectionWatcher optionalWatcher, boolean clientSide) throws IOException {
        return connect(inetadd, port, timeout, optionalWatcher, clientSide, -1);
    }

    /**
     * Return a client socket, timing out if unable to connect and timeout > 0 (millis). The parameter
     * <i>timeout</i> is ignored if SSL is being used, as there is no timeout argument in the ssl
     * socket factory
     */
    public Socket connect(InetAddress inetadd, int port, int timeout,
                          ConnectionWatcher optionalWatcher, boolean clientSide, int socketBufferSize)
            throws IOException {
        return connect(inetadd, port, timeout, optionalWatcher, clientSide, socketBufferSize,
                false);
    }

    /**
     * Return a client socket, timing out if unable to connect and timeout > 0 (millis). The parameter
     * <i>timeout</i> is ignored if SSL is being used, as there is no timeout argument in the ssl
     * socket factory
     */
    public Socket connect(InetAddress inetadd, int port, int timeout,
                          ConnectionWatcher optionalWatcher, boolean clientSide, int socketBufferSize,
                          boolean sslConnection) throws IOException {
        Socket socket = null;
        SocketAddress sockaddr = new InetSocketAddress(inetadd, port);
        try {


                socket = new Socket();

                // Optionally enable SO_KEEPALIVE in the OS network protocol.
                socket.setKeepAlive(ENABLE_TCP_KEEP_ALIVE);

                // If necessary, set the receive buffer size before connecting the
                // socket so that large buffers will be allocated on accepted sockets
                // (see java.net.Socket.setReceiverBufferSize javadocs for details)
                if (socketBufferSize != -1) {
                    socket.setReceiveBufferSize(socketBufferSize);
                }

                if (optionalWatcher != null) {
                    optionalWatcher.beforeConnect(socket);
                }
                socket.connect(sockaddr, Math.max(timeout, 0));

            return socket;

        } finally {
            if (optionalWatcher != null) {
                optionalWatcher.afterConnect(socket);
            }
        }
    }

}


