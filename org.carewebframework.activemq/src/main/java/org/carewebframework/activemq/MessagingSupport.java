/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.carewebframework.activemq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.carewebframework.activemq.EventUtil;
import org.carewebframework.api.event.EventManager;

import org.springframework.jms.core.MessageCreator;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 *
 */
@ManagedResource(description = "Runtime messaging support.")
public class MessagingSupport {
    
    /**
     * Produces local message. Uses EventManager.
     * 
     * @param eventName The destination
     * @param messageData The message data
     */
    public void produceLocalMessage(final String eventName, final Object messageData) {
        EventManager.getInstance().fireLocalEvent(eventName, messageData);
    }
    
    /**
     * Produces topic message. Uses JmsTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param eventName The event name for which the destination (Topic) is derived.
     * @param messageData The message data
     * @param recipients Comma delimited list of clientIDs (JMS Connection Clients)
     */
    @ManagedOperation(description = "Produces topic message. Uses JmsTemplate to send to local broker and forwards (based on demand) to broker network.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "eventName", description = "The event name for which the destination (Topic) is derived."),
            @ManagedOperationParameter(name = "messageData", description = "The message data"),
            @ManagedOperationParameter(name = "recipients", description = "Comma delimited list of clientIDs (JMS Connection Clients)") })
    public void produceTopicMessage(final String eventName, final String messageData, final String recipients) {
        EventUtil.getJmsTopicTemplate().send(EventUtil.getTopicName(eventName), new MessageCreator() {
            
            @Override
            public Message createMessage(final Session session) throws JMSException {
                return EventUtil.createObjectMessage(session, eventName, messageData, recipients);
            }
        });
    }
    
    /**
     * Produces queue message. Uses JmsTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param destinationName The destination name.
     * @param messageData The message data
     */
    @ManagedOperation(description = "Produces queue message. Uses JmsTemplate to send to local broker and forwards (based on demand) to broker network.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "destinationName", description = "The destination name."),
            @ManagedOperationParameter(name = "messageData", description = "The message data") })
    public void produceQueueMessage(final String destinationName, final String messageData) {
        EventUtil.getJmsQueueTemplate().send(destinationName, new MessageCreator() {
            
            @Override
            public Message createMessage(final Session session) throws JMSException {
                return EventUtil.createObjectMessage(session, destinationName, messageData, null);
            }
        });
    }
    
}
