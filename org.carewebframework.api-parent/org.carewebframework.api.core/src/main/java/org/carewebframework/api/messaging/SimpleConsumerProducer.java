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
 * This is a simple implementation of a message consumer and producer that does not require a
 * messaging framework.
 */
public class SimpleConsumerProducer implements IMessageProducer, IMessageConsumer {
    
    private IMessageCallback callback;
    
    @Override
    public void setCallback(IMessageCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public boolean subscribe(String channel) {
        return true;
    }
    
    @Override
    public boolean unsubscribe(String channel) {
        return true;
    }
    
    @Override
    public boolean publish(Message message) {
        callback.onMessage(message);
        return true;
    }
    
}
