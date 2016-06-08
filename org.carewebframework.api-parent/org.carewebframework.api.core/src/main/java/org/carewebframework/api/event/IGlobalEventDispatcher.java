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
