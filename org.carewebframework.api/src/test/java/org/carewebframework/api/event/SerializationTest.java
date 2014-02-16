/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.event.PingFilter.PingFilterType;
import org.carewebframework.common.JSONUtil;

import org.junit.Test;

public class SerializationTest {
    
    @Test
    public void testSerialization() {
        List<PingFilter> filters = new ArrayList<PingFilter>();
        filters.add(new PingFilter(PingFilterType.APP_NAME, "testApp"));
        filters.add(new PingFilter(PingFilterType.SENTINEL_EVENT, "testEvent"));
        PingRequest pingRequest = new PingRequest("TEST.RESPONSE", filters, "testRequestor");
        String data = JSONUtil.serialize(pingRequest);
        pingRequest = (PingRequest) JSONUtil.deserialize(data);
        assertEquals("TEST.RESPONSE", pingRequest.responseEvent);
        assertEquals(filters, pingRequest.filters);
        assertEquals("testRequestor", pingRequest.requestor);
        
        PublisherInfo publisherInfo = new PublisherInfo();
        publisherInfo.setAppName("testApp");
        publisherInfo.setEndpointId("testEP");
        publisherInfo.setNodeId("testNode");
        publisherInfo.setUserId("testUserId");
        publisherInfo.setUserName("testUser");
        data = JSONUtil.serialize(publisherInfo);
        publisherInfo = (PublisherInfo) JSONUtil.deserialize(data);
        assertEquals("a-testApp", publisherInfo.getAppName());
        assertEquals("testEP", publisherInfo.getEndpointId());
        assertEquals("n-testNode", publisherInfo.getNodeId());
        assertEquals("u-testUserId", publisherInfo.getUserId());
        assertEquals("testUser", publisherInfo.getUserName());
    }
}
