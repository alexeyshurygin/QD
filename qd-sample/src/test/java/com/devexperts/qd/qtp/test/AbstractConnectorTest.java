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
package com.devexperts.qd.qtp.test;

import com.devexperts.logging.Logging;
import com.devexperts.qd.qtp.MessageConnector;
import com.devexperts.qd.qtp.MessageConnectorState;
import com.devexperts.qd.qtp.socket.ClientSocketConnector;
import com.devexperts.qd.qtp.socket.ServerSocketTestHelper;
import com.devexperts.test.ThreadCleanCheck;
import com.devexperts.test.TraceRunner;
import com.dxfeed.api.DXEndpoint;
import com.dxfeed.api.DXFeed;
import com.dxfeed.api.DXFeedEventListener;
import com.dxfeed.api.DXFeedSubscription;
import com.dxfeed.api.DXPublisher;
import com.dxfeed.api.impl.DXEndpointImpl;
import com.dxfeed.event.misc.Message;
import com.dxfeed.promise.Promise;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(TraceRunner.class)
public abstract class AbstractConnectorTest<E> {

    protected final Logging log = Logging.getLogging(getClass());

    protected static final int WAIT_TIMEOUT = 10; // seconds

    protected String testId;

    protected int port;
    protected DXEndpoint pubEndpoint;
    protected DXEndpoint feedEndpoint;
    protected BlockingQueue<E> events;

    @Before
    public void setUp() throws Exception {
        ThreadCleanCheck.before();
        testId = UUID.randomUUID().toString();
    }

    @After
    public void tearDown() throws Exception {
        log.info("======== tearDown ========");
        if (feedEndpoint != null)
            feedEndpoint.close();
        if (pubEndpoint != null)
            pubEndpoint.close();
        ThreadCleanCheck.after();
    }

    protected ClientSocketConnector getClientSocketConnector(DXEndpoint feedEndpoint) {
        List<MessageConnector> connectors = ((DXEndpointImpl) feedEndpoint).getQDEndpoint().getConnectors();
        assertEquals(1, connectors.size());
        assertTrue(connectors.get(0) instanceof ClientSocketConnector);
        return (ClientSocketConnector) connectors.get(0);
    }

    protected void startPublisher(String symbol) {
        String name = testId + "-pub-" + symbol;
        Promise<Integer> port = ServerSocketTestHelper.createPortPromise(name);
        DXEndpoint endpoint = DXEndpoint.newBuilder()
            .withRole(DXEndpoint.Role.PUBLISHER)
            .build()
            .connect(":0[name=" + name + ",bindAddr=127.0.0.1]");
        DXPublisher publisher = endpoint.getPublisher();
        publisher.getSubscription(Message.class)
            .addChangeListener((symbols) -> {
                publisher.publishEvents(Collections.singleton(new Message(symbol)));
            });
        this.pubEndpoint = endpoint;
        this.port = port.await(WAIT_TIMEOUT, TimeUnit.SECONDS);
    }

    protected DXEndpoint createFeedEndpoint(String symbol, DXFeedEventListener<E> listener, Class<E> clazz) {
        DXEndpoint endpoint = DXEndpoint.create(DXEndpoint.Role.FEED);
        DXFeed feed = endpoint.getFeed();

        DXFeedSubscription<E> subscription = feed.createSubscription(clazz);
        subscription.addEventListener(listener);
        subscription.addSymbols(symbol);
        return endpoint;
    }

    protected void waitForConnectionCount(MessageConnector connector, int count) throws InterruptedException {
        long timeoutTime = System.currentTimeMillis() + WAIT_TIMEOUT * 1_000L;
        while (connector.getState() != MessageConnectorState.CONNECTED || connector.getConnectionCount() != count) {
            Thread.sleep(10);
            if (System.currentTimeMillis() > timeoutTime) {
                fail("Test timeout, couldn't connect to endpoint");
            }
        }
    }
}
