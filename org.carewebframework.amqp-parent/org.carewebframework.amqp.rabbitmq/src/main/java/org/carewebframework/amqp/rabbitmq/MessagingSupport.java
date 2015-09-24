/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.amqp.rabbitmq;

import org.carewebframework.api.event.EventManager;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * JMX-based messaging support.
 */
@ManagedResource(description = "Runtime messaging support.")
public class MessagingSupport {
    
    private final Broker broker;
    
    /**
     * @param broker The AMQP broker.
     */
    public MessagingSupport(Broker broker) {
        this.broker = broker;
    }
    
    /**
     * Produces local message. Uses EventManager.
     * 
     * @param eventName The destination.
     * @param messageData The message data.
     */
    public void produceLocalMessage(String eventName, Object messageData) {
        EventManager.getInstance().fireLocalEvent(eventName, messageData);
    }
    
    /**
     * Produces topic message. Uses AmqpTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param eventName The event name for which the destination (Topic) is derived.
     * @param eventData The event data.
     * @param recipients Comma-delimited list of recipient ids.
     */
    @ManagedOperation(description = "Produces a message. Uses AMQP broker to send.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "eventName", description = "The event name from which the destination (Topic) is derived."),
            @ManagedOperationParameter(name = "eventData", description = "The event data"),
            @ManagedOperationParameter(name = "recipients", description = "Comma delimited list of recipient ids") })
    public void produceMessage(String eventName, String eventData, String recipients) {
        broker.sendEvent(eventName, eventData, "anonymous", recipients);
    }
    
}
