/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.carewebframework.api.event.EventManager;

import org.springframework.jms.core.MessageCreator;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * JMX-based messaging support.
 */
@ManagedResource(description = "Runtime messaging support.")
public class MessagingSupport {
    
    /**
     * Produces local message. Uses EventManager.
     * 
     * @param eventName The destination.
     * @param messageData The message data.
     */
    public void produceLocalMessage(final String eventName, final Object messageData) {
        EventManager.getInstance().fireLocalEvent(eventName, messageData);
    }
    
    /**
     * Produces topic message. Uses JmsTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param eventName The event name for which the destination (Topic) is derived.
     * @param messageData The message data.
     * @param recipients Comma-delimited list of clientIDs (JMS Connection Clients).
     */
    @ManagedOperation(description = "Produces topic message. Uses JmsTemplate to send to local broker and forwards (based on demand) to broker network.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "eventName", description = "The event name for which the destination (Topic) is derived."),
            @ManagedOperationParameter(name = "messageData", description = "The message data"),
            @ManagedOperationParameter(name = "recipients", description = "Comma delimited list of clientIDs (JMS Connection Clients)") })
    public void produceTopicMessage(final String eventName, final String messageData, final String recipients) {
        JMSUtil.getJmsTopicTemplate().send(JMSUtil.getTopicName(eventName), new MessageCreator() {
            
            @Override
            public Message createMessage(final Session session) throws JMSException {
                return JMSUtil.createObjectMessage(session, eventName, messageData, "anonymous", recipients);
            }
        });
    }
    
    /**
     * Produces queue message. Uses JmsTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param destinationName The destination name.
     * @param messageData The message data.
     */
    @ManagedOperation(description = "Produces queue message. Uses JmsTemplate to send to local broker and forwards (based on demand) to broker network.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "destinationName", description = "The destination name."),
            @ManagedOperationParameter(name = "messageData", description = "The message data") })
    public void produceQueueMessage(final String destinationName, final String messageData) {
        JMSUtil.getJmsQueueTemplate().send(destinationName, new MessageCreator() {
            
            @Override
            public Message createMessage(final Session session) throws JMSException {
                return JMSUtil.createObjectMessage(session, destinationName, messageData, "anonymous", null);
            }
        });
    }
    
}
