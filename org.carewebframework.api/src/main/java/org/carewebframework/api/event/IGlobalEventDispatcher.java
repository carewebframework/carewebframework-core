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
     * @throws Exception
     */
    void subscribeRemoteEvent(String eventName, boolean subscribe) throws Exception;
    
    /**
     * Queues the specified event for delivery via the message server.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients List of recipients for the event (null or empty string means all
     *            subscribers).
     * @throws Exception
     */
    void fireRemoteEvent(String eventName, Serializable eventData, String recipients) throws Exception;
}
