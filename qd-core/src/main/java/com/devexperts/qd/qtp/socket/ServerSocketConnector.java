/*
 * !++
 * QDS - Quick Data Signalling Library
 * !-
 * Copyright (C) 2002 - 2023 Devexperts LLC
 * !-
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * !__
 */
package com.devexperts.qd.qtp.socket;

import com.devexperts.connector.codec.CodecConnectionFactory;
import com.devexperts.connector.codec.CodecFactory;
import com.devexperts.connector.proto.ApplicationConnectionFactory;
import com.devexperts.connector.proto.ConfigurationKey;
import com.devexperts.qd.QDFactory;
import com.devexperts.qd.qtp.AbstractMessageConnector;
import com.devexperts.qd.qtp.MessageAdapter;
import com.devexperts.qd.qtp.MessageConnector;
import com.devexperts.qd.qtp.MessageConnectorState;
import com.devexperts.qd.qtp.MessageConnectors;
import com.devexperts.qd.qtp.help.MessageConnectorProperty;
import com.devexperts.qd.qtp.help.MessageConnectorSummary;
import com.devexperts.qd.stats.QDStats;
import com.devexperts.qd.util.QDConfig;
import com.devexperts.services.Services;
import com.devexperts.transport.stats.ConnectionStats;
import com.devexperts.transport.stats.EndpointStats;
import com.devexperts.util.LogUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>ServerSocketConnector</code> handles standard server socket using blocking API.
 */
@MessageConnectorSummary(
    info = "Creates server TCP/IP socket connection.",
    addressFormat = ":<port>"
)
public class ServerSocketConnector extends AbstractMessageConnector implements ServerSocketConnectorMBean {
    private static final String BIND_ANY_ADDRESS = "*";

    protected int port;
    protected String bindAddrString = BIND_ANY_ADDRESS;
    protected InetAddress bindAddr;
    protected boolean useTls;
    // 0 stands for unlimited number of connections
    protected int maxConnections;

    protected final Set<SocketHandler> handlers = new HashSet<>();
    protected final SocketHandler.CloseListener closeListener = this::handlerClosed;

    protected volatile SocketAcceptor acceptor;

    /**
     * Creates new server socket connector.
     *
     * @deprecated use {@link #ServerSocketConnector(ApplicationConnectionFactory, int)}
     * @param factory message adapter factory to use
     * @param port TCP port to use
     * @throws NullPointerException if {@code factory} is {@code null}
     */
    @SuppressWarnings({"deprecation", "UnusedDeclaration"})
    @Deprecated
    public ServerSocketConnector(MessageAdapter.Factory factory, int port) {
        this(MessageConnectors.applicationConnectionFactory(factory), port);
    }

    /**
     * Creates new server socket connector.
     *
     * @param factory application connection factory to use
     * @param port TCP port to use
     * @throws NullPointerException if {@code factory} is {@code null}
     */
    public ServerSocketConnector(ApplicationConnectionFactory factory, int port) {
        super(factory);
        QDConfig.setDefaultProperties(this, ServerSocketConnectorMBean.class, MessageConnector.class.getName());
        QDConfig.setDefaultProperties(this, ServerSocketConnectorMBean.class, ServerSocketConnector.class.getName());
        this.port = port;
    }

    @Override
    public String getAddress() {
        return bindAddrString + ":" + port;
    }

    /**
     * Changes local port and restarts connector
     * if new port is different from the old one and the connector was running.
     */
    @Override
    public synchronized void setLocalPort(int port) {
        if (this.port != port) {
            log.info("Setting localPort=" + port);
            this.port = port;
            reconfigure();
        }
    }

    @Override
    public int getLocalPort() {
        return port;
    }

    @Override
    public String getBindAddr() {
        return bindAddrString;
    }

    @Override
    @MessageConnectorProperty("Network interface address to bind socket to")
    public synchronized void setBindAddr(String bindAddrString) throws UnknownHostException {
        if (bindAddrString == null)
            bindAddrString = BIND_ANY_ADDRESS;
        if (!bindAddrString.equals(this.bindAddrString)) {
            log.info("Setting bindAddr=" + bindAddrString);
            this.bindAddr = bindAddrString.isEmpty() ? null : InetAddress.getByName(bindAddrString);
            this.bindAddrString = bindAddrString;
            reconfigure();
        }
    }

    @Override
    public int getMaxConnections() {
        return maxConnections;
    }

    @Override
    @MessageConnectorProperty("Max number of connections allowed for connector")
    public synchronized void setMaxConnections(int maxConnections) {
        if (maxConnections != this.maxConnections) {
            log.info("Setting maxConnections=" + maxConnections);
            this.maxConnections = maxConnections;
            reconfigure();
        }
    }

    public boolean getTls() {
        return useTls;
    }

    @MessageConnectorProperty(
        value = "Use SSLConnectionFactory",
        deprecated = "Use tls or ssl codec in address string. For example tls+<address>"
    )
    public synchronized void setTls(boolean useTls) {
        if (this.useTls != useTls) {
            if (useTls) {
                CodecFactory sslCodecFactory = Services.createService(CodecFactory.class, null, "com.devexperts.connector.codec.ssl.SSLCodecFactory");
                if (sslCodecFactory == null) {
                    log.error("SSLCodecFactory is not found. Using the SSL protocol is not supported");
                    return;
                }
                ApplicationConnectionFactory factory = sslCodecFactory.createCodec("ssl", getFactory());
                factory.setConfiguration(ConfigurationKey.create("isServer", String.class), "true");
                setFactory(factory);
            } else {
                CodecConnectionFactory sslFactory = (CodecConnectionFactory) getFactory();
                if (!sslFactory.getClass().getSimpleName().contains("SSLCodecFactory")) {
                    log.error("SSLCodecFactory not found. SSL protocol is not used");
                    return;
                }
                setFactory(sslFactory.getDelegate());
            }
            log.info("Setting useTls=" + useTls);
            this.useTls = useTls;
            reconfigure();
        }
        log.warn("WARNING: DEPRECATED use \"setTls()\" method from program or \"tls\" property from address string. " +
            "Use tls or ssl codec in address string. For example tls+<address>");
    }

    /**
     * Sets stats for this connector. Stats should be of type {@link QDStats.SType#SERVER_SOCKET_CONNECTOR} or
     * a suitable substitute. This method may be invoked only once.
     *
     * @throws IllegalStateException if already set.
     */
    @Override
    public void setStats(QDStats stats) {
        super.setStats(stats);
        stats.addMBean("ServerSocketConnector", this);
    }

    @Override
    public boolean isActive() {
        return acceptor != null;
    }

    @Override
    public MessageConnectorState getState() {
        SocketAcceptor acceptor = this.acceptor;
        if (acceptor == null)
            return MessageConnectorState.DISCONNECTED;
        return acceptor.isConnected() ? MessageConnectorState.CONNECTED : MessageConnectorState.CONNECTING;
    }

    @Override
    public synchronized int getConnectionCount() {
        return handlers.size();
    }

    @Override
    public synchronized EndpointStats retrieveCompleteEndpointStats() {
        EndpointStats stats = super.retrieveCompleteEndpointStats();
        for (SocketHandler handler : handlers) {
            ConnectionStats connectionStats = handler.getActiveConnectionStats(); // Atomic read.
            if (connectionStats != null) {
                stats.addActiveConnectionCount(1);
                stats.addConnectionStats(connectionStats);
            }
        }
        return stats;
    }

    @Override
    public synchronized void start() {
        if (acceptor != null)
            return;
        log.info("Starting ServerSocketConnector to " + LogUtil.hideCredentials(getAddress()));
        // create default stats instance if specific one was not provided.
        if (getStats() == null)
            setStats(QDFactory.getDefaultFactory().createStats(QDStats.SType.SERVER_SOCKET_CONNECTOR, null));
        acceptor = new SocketAcceptor(this);
        acceptor.start();
    }

    @Override
    protected synchronized Joinable stopImpl() {
        SocketAcceptor acceptor = this.acceptor;
        if (acceptor == null)
            return null;
        log.info("Stopping ServerSocketConnector");
        // Note, that the order of below two invocations is important to handle concurrent stop during the
        // creation of ServerSocket that listens on the specified port.
        //      SocketAcceptor.close            modifies SocketAcceptor.closed field,
        // then SocketAcceptor.closeSocketImpl  modified SocketAcceptor.serverSocket field.
        // SocketAcceptor.doWork method accesses the above fields in reverse order
        acceptor.close();
        acceptor.closeSocketImpl(null);
        this.acceptor = null;
        SocketHandler[] a = handlers.toArray(new SocketHandler[handlers.size()]);
        for (int i = a.length; --i >= 0;)
            a[i].close();
        return new Stopped(acceptor, a);
    }

    private static class Stopped implements Joinable {
        private final SocketAcceptor acceptor;
        private final SocketHandler[] a;

        Stopped(SocketAcceptor acceptor, SocketHandler[] a) {
            this.acceptor = acceptor;
            this.a = a;
        }

        @Override
        public void join() throws InterruptedException {
            acceptor.join();
            for (SocketHandler handler : a)
                handler.join();
        }
    }

    protected synchronized void addHandler(SocketHandler handler) {
        if (acceptor == null)
            handler.close(); // in case of close/connect race.
        else
            handlers.add(handler);
    }

    protected synchronized void handlerClosed(SocketHandler handler) {
        handlers.remove(handler);
    }

    protected synchronized boolean isNewConnectionAllowed() {
        return maxConnections == 0 || getConnectionCount() < maxConnections;
    }
}
