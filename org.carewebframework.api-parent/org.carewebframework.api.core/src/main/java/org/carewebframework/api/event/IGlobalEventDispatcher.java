/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.api.event;

import java.io.Serializable;
import java.util.List;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.api.messaging.Recipient;

/**
 * Interface implemented by the global event dispatcher.
 */
public interface IGlobalEventDispatcher {
    
    /**
     * Returns information about this publisher.
     * 
     * @return Publisher information.
     */
    IPublisherInfo getPublisherInfo();
    
    /**
     * Request or revoke a global event subscription.
     * 
     * @param eventName The name of the event of interest.
     * @param subscribe If true, a subscription is requested. If false, it is revoked.
     */
    void subscribeRemoteEvent(String eventName, boolean subscribe);
    
    /**
     * Queues the specified event for delivery via the messaging service.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients Optional list of recipients for the event.
     */
    void fireRemoteEvent(String eventName, Serializable eventData, Recipient... recipients);
    
    /**
     * @param responseEvent
     * @param filters
     * @param recipients
     */
    void Ping(String responseEvent, List<PingFilter> filters, Recipient... recipients);
}
