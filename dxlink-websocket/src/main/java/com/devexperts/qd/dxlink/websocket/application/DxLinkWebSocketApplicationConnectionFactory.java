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
package com.devexperts.qd.dxlink.websocket.application;

import com.devexperts.connector.proto.ApplicationConnection;
import com.devexperts.connector.proto.ApplicationConnectionFactory;
import com.devexperts.connector.proto.Configurable;
import com.devexperts.connector.proto.ConfigurationKey;
import com.devexperts.connector.proto.TransportConnection;
import com.devexperts.qd.QDFactory;
import com.devexperts.qd.dxlink.websocket.transport.DxLinkLoginHandlerFactory;
import com.devexperts.qd.dxlink.websocket.transport.TokenDxLinkLoginHandlerFactory;
import com.devexperts.qd.qtp.MessageAdapter;
import com.devexperts.qd.qtp.MessageConnectors;
import com.devexperts.qd.qtp.auth.QDLoginHandler;
import com.devexperts.qd.stats.QDStats;
import com.devexperts.services.Services;
import com.devexperts.util.SystemProperties;
import com.devexperts.util.TimePeriod;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * DxLink Connection protocol implementation.
 */
public class DxLinkWebSocketApplicationConnectionFactory extends ApplicationConnectionFactory {
    private static final String APPLICATION_VERSION =
        SystemProperties.getProperty("com.devexperts.qd.dxlink.applicationVersion", null);
    private static final TimePeriod DEFAULT_HEARTBEAT_TIMEOUT = TimePeriod.valueOf(
        SystemProperties.getProperty("com.devexperts.qd.dxlink.websocket.heartbeatTimeout", "60s"));

    private TimePeriod heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    private String applicationVersion = APPLICATION_VERSION;
    private MessageAdapter.ConfigurableFactory factory;
    private QDLoginHandler loginHandler;

    public DxLinkWebSocketApplicationConnectionFactory(MessageAdapter.ConfigurableFactory factory) {
        if (factory == null)
            throw new NullPointerException();
        this.factory = factory;
    }

    @Override
    public ApplicationConnection<?> createConnection(TransportConnection transportConnection) {
        QDStats stats = transportConnection.variables().get(MessageConnectors.STATS_KEY);
        if (stats == null)
            stats = QDStats.VOID;

        MessageAdapter adapter = this.factory.createAdapter(stats);
        adapter.setConnectionVariables(transportConnection.variables());
        adapter.setLoginHandler(loginHandler);
        adapter.useDescribeProtocol();

        HeartbeatProcessor heartbeatProcessor = new HeartbeatProcessor(getHeartbeatTimeout().getTime());

        Delegates delegates = new Delegates(adapter.getScheme());

        return new DxLinkWebSocketApplicationConnection(
            adapter,
            this,
            transportConnection,
            new DxLinkWebSocketQTPParser(
                adapter.getScheme(),
                adapter.supportsMixedSubscription(),
                adapter.getFieldReplacer(),
                heartbeatProcessor,
                receiver -> new DxLinkJsonMessageParser(receiver, delegates)
            ),
            new DxLinkWebSocketQTPComposer(
                adapter.getScheme(),
                delegates,
                new DxLinkJsonMessageFactory(),
                heartbeatProcessor,
                this
            ),
            heartbeatProcessor
        );
    }

    @Override
    public DxLinkWebSocketApplicationConnectionFactory clone() {
        DxLinkWebSocketApplicationConnectionFactory clone = (DxLinkWebSocketApplicationConnectionFactory) super.clone();
        clone.factory = factory.clone();
        return clone;
    }

    @Override
    public Set<ConfigurationKey<?>> supportedConfiguration() {
        Set<ConfigurationKey<?>> set = new LinkedHashSet<>(super.supportedConfiguration());
        set.addAll(factory.supportedConfiguration());
        return set;
    }

    @Override
    public <T> T getConfiguration(ConfigurationKey<T> key) {
        if (super.supportedConfiguration().contains(key))
            return super.getConfiguration(key);
        return factory.getConfiguration(key);
    }

    @Override
    public <T> boolean setConfiguration(ConfigurationKey<T> key, T value) {
        if (super.supportedConfiguration().contains(key))
            return super.setConfiguration(key, value);
        return factory.setConfiguration(key, value);
    }

    @Configurable(description = "Login plugin")
    public void setLogin(String login) {
        for (DxLinkLoginHandlerFactory factory :
            Services.createServices(DxLinkLoginHandlerFactory.class, null))
        {
            this.loginHandler = factory.createLoginHandler(login, this);
            if (this.loginHandler != null)
                return;
        }
        this.loginHandler = TokenDxLinkLoginHandlerFactory.INSTANCE.createLoginHandler(login, this);
    }

    public QDLoginHandler getLogin() { return loginHandler; }

    /**
     * Returns heartbeat timeout for this application protocol.
     * @return heartbeat timeout for this application protocol
     */
    public TimePeriod getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    @Configurable(description = "heartbeat timeout for this connection")
    public void setHeartbeatTimeout(TimePeriod heartbeatTimeout) {
        if (heartbeatTimeout.getTime() <= 0)
            throw new IllegalArgumentException("cannot be negative or zero");
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @Configurable(description = "client application version")
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Map<String, String> getAgentInfo() {
        Map<String, String> agent = new TreeMap<>();
        agent.put("version", QDFactory.getVersion());
        if (getApplicationVersion() != null)
            agent.put("application", getApplicationVersion());
        agent.put("platform", SystemProperties.getProperty("os.name", null) + " " +
            SystemProperties.getProperty("os.version", null));
        String javaVersion = SystemProperties.getProperty("java.version", null);
        if (javaVersion != null)
            agent.put("java", javaVersion);
        return agent;
    }

    public String toString() {
        return factory.toString();
    }
}
