/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.messaging;

/**
 * Interface to be implemented by every message consumer.
 */
public interface IMessageConsumer {
    
    /**
     * Callback for asynchronous message delivery.
     */
    interface IMessageCallback {
        
        void onMessage(Message message);
    }
    
    /**
     * Sets the callback method for processing received messages.
     * 
     * @param callback Callback method.
     */
    void setCallback(IMessageCallback callback);
    
    /**
     * Subscribe to the specified channel.
     * 
     * @param channel The channel.
     * @return True if not already subscribed.
     */
    boolean subscribe(String channel);
    
    /**
     * Unsubscribe from the specified channel.
     * 
     * @param channel The channel.
     * @return False if not already subscribed.
     */
    boolean unsubscribe(String channel);
}
