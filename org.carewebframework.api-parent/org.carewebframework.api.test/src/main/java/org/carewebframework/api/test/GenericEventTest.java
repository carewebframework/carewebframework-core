/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.event.IPublisherInfo;
import org.junit.Test;

public class GenericEventTest extends CommonTest {
    
    private static final Log log = LogFactory.getLog(GenericEventTest.class);
    
    private static final String EVENT_NAME1 = "Event";
    
    private static final String EVENT_NAME2 = "Event.Subevent";
    
    protected static int pollingInterval = 500;
    
    private int eventCount;
    
    private int expectedUndelivered;
    
    private boolean pingResponded;
    
    protected boolean remote;
    
    protected String recipients;
    
    private final List<TestPacket> tests = Collections.synchronizedList(new ArrayList<>());
    
    protected String recipientId;
    
    private AssertionError assertionError;
    
    private final IGenericEvent<TestPacket> subscriber = new IGenericEvent<TestPacket>() {
        
        @Override
        public void eventCallback(String eventName, TestPacket testPacket) {
            try {
                log.info("Received: " + testPacket);
                assertTrue(testPacket + ": unexpected test packet", tests.remove(testPacket));
                assertTrue(testPacket + ": name does not match.", testPacket.getEventName().equals(eventName));
                assertTrue(testPacket + ": should not have been received.", testPacket.isShouldReceive());
            } catch (AssertionError e) {
                if (assertionError == null) {
                    assertionError = e;
                }
            }
        }
    };
    
    private final IGenericEvent<IPublisherInfo> pingSubscriber = new IGenericEvent<IPublisherInfo>() {
        
        @Override
        public void eventCallback(String eventName, IPublisherInfo publisherInfo) {
            log.info(publisherInfo);
            pingResponded = true;
        }
        
    };
    
    /**
     * Fire the specified event.
     * 
     * @param eventName Name of the event to fire.
     * @param shouldReceive Indicates whether or not the event should be received.
     */
    private void fireEvent(String eventName, boolean shouldReceive) {
        TestPacket testPacket = new TestPacket(++eventCount, eventName, shouldReceive, remote);
        tests.add(testPacket);
        
        if (!shouldReceive) {
            expectedUndelivered++;
        }
        
        log.info("Sending: " + testPacket);
        
        if (remote) {
            eventManager.fireRemoteEvent(eventName, testPacket, recipients);
        } else {
            eventManager.fireLocalEvent(eventName, testPacket);
        }
    }
    
    public GenericEventTest() {
        remote = false;
        recipients = null;
    }
    
    @Test
    public void testEvents() {
        if (remote) {
            pingTest();
        }
        
        fireTestEvents();
        assertEquals("Error testing ping response.", remote, pingResponded);
    }
    
    public void pingTest() {
        eventManager.subscribe("PING.TEST", pingSubscriber);
        EventUtil.ping("PING.TEST", null, recipients);
    }
    
    private void fireTestEvents() {
        //content based routing via Message Selector tests
        fireEvent(EVENT_NAME1, false); // Subscriber not registered, should not receive
        fireEvent(EVENT_NAME2, false); // Subevent should also not be received
        subscribe(EVENT_NAME1, true);
        fireEvent(EVENT_NAME1, true); // Subscriber registered, should receive
        fireEvent(EVENT_NAME2, true); // Subevent should also be received
        if (remote) {
            //Additional routing options / recipient filtering
            recipients = recipientId;
            fireEvent(EVENT_NAME1, true); // Subscriber registered, should receive due to defined/intended recipients
            fireEvent(EVENT_NAME2, true); // Subevent w/ defined/intended recipients should also be received
            //not null bogus recipients
            recipients = "alternateClient";
            fireEvent(EVENT_NAME1, false); // Subscriber registered, but should not receive due to defined unintended recipients
            fireEvent(EVENT_NAME2, false); // Subevent should not also be received
            recipients = null;
        }
        subscribe(EVENT_NAME1, false);
        fireEvent(EVENT_NAME1, false); // Subscriber not registered, should not receive
        fireEvent(EVENT_NAME2, false); // Subevent should also not be received
        subscribe(EVENT_NAME2, true);
        fireEvent(EVENT_NAME1, false); // Subscriber not registered, should not receive
        fireEvent(EVENT_NAME2, true); // Subevent should be received
        if (remote) {
            //Additional routing options / recipient filtering
            recipients = recipientId;
            fireEvent(EVENT_NAME1, false); // Subscriber registered, should receive due to defined/intended recipients
            fireEvent(EVENT_NAME2, true); // Subevent w/ defined/intended recipients should also be received
            //not null bogus recipients
            recipients = "alternateClient";
            fireEvent(EVENT_NAME1, false); // Subscriber registered, but should not receive due to defined unintended recipients
            fireEvent(EVENT_NAME2, false); // Subevent should not also be received
            recipients = null;
        }
        subscribe(EVENT_NAME1, true);
        subscribe(EVENT_NAME2, false);
        fireEvent(EVENT_NAME1, true); // Subscriber registered, should receive
        fireEvent(EVENT_NAME2, true); // Subevent should also be received
        if (remote) {
            //Additional routing options / recipient filtering
            recipients = recipientId;
            fireEvent(EVENT_NAME1, true); // Subscriber registered, should receive due to defined/intended recipients
            fireEvent(EVENT_NAME2, true); // Subevent w/ defined/intended recipients should also be received
            //not null bogus recipients
            recipients = "alternateClient";
            fireEvent(EVENT_NAME1, false); // Subscriber registered, but should not receive due to defined unintended recipients
            fireEvent(EVENT_NAME2, false); // Subevent should not also be received
            recipients = null;
        }
        subscribe(EVENT_NAME1, false);
        fireEvent(EVENT_NAME1, false); // Subscriber not registered, should not receive
        fireEvent(EVENT_NAME2, false); // Subevent should also not be received
        undeliveredEvents(remote);
        checkAssertion();
    }
    
    private void checkAssertion() {
        if (assertionError != null) {
            throw assertionError;
        }
    }
    
    private void subscribe(String eventName, boolean subscribe) {
        if (remote) {
            doWait(30);
        }
        
        if (subscribe) {
            eventManager.subscribe(eventName, subscriber);
        } else {
            eventManager.unsubscribe(eventName, subscriber);
        }
    }
    
    private void doWait(int count) {
        while ((count-- > 0) && (tests.size() > expectedUndelivered)) {
            try {
                Thread.sleep(pollingInterval);
            } catch (InterruptedException e) {
                continue;
            }
        }
        
        assertFalse("Timed out waiting for packet delivery.", tests.size() > expectedUndelivered);
    }
    
    /**
     * Verify all undelivered events.
     * 
     * @param wait If true, wait before verifying.
     */
    private void undeliveredEvents(boolean wait) {
        if (wait) {
            doWait(20);
        }
        
        while (!tests.isEmpty()) {
            TestPacket testPacket = tests.remove(0);
            assertFalse(testPacket + ": was not received.", testPacket.isShouldReceive());
        }
        
        tests.clear();
    }
}
