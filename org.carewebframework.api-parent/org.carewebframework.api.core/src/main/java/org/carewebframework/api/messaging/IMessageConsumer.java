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
package org.carewebframework.api.messaging;

/**
 * Interface to be implemented by every message consumer.
 */
public interface IMessageConsumer {
    
    /**
     * Callback for asynchronous message delivery.
     */
    interface IMessageCallback {
        
        void onMessage(String channel, Message message);
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
