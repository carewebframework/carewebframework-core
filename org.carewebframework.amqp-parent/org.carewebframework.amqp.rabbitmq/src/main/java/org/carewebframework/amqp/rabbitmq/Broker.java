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

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

/**
 * AMQP broker administration.
 */
public class Broker extends RabbitAdmin {
    
    protected static final String RECIPIENTS_PROPERTY = "recipients";
    
    protected static final String SENDER_PROPERTY = "sender";
    
    private final Exchange exchange;
    
    /**
     * Creates a broker instance with the specified connection factory and default exchange.
     * 
     * @param connectionFactory Connection factory.
     * @param exchange Default exchange for message delivery.
     */
    public Broker(ConnectionFactory connectionFactory, Exchange exchange) {
        super(connectionFactory);
        this.exchange = exchange;
    }
    
    /**
     * Returns the default exchange.
     * 
     * @return The default exchange.
     */
    public Exchange getExchange() {
        return exchange;
    }
    
    /**
     * Declares an event queue if one does not exist.
     * 
     * @param eventName Name of event handled by queue.
     */
    public void declareEventQueue(String eventName) {
        if (!queueExists(eventName)) {
            createEventQueue(eventName);
        }
    }
    
    /**
     * Returns true if the named queue already exists.
     * 
     * @param queueName The queue name.
     * @return True if the queue exists.
     */
    private boolean queueExists(String queueName) {
        return getQueueProperties(queueName) != null;
    }
    
    /**
     * Creates an event queue (thread safe) with the correct binding.
     * 
     * @param eventName Name of event handled by queue.
     */
    private synchronized void createEventQueue(String eventName) {
        if (!queueExists(eventName)) {
            Queue queue = new Queue(eventName, true, false, true);
            declareQueue(queue);
            Binding binding = new Binding(eventName, DestinationType.QUEUE, exchange.getName(), eventName + ".#", null);
            declareBinding(binding);
        }
    }
    
    /**
     * Sends an event to the default exchange.
     * 
     * @param eventName Name of the event.
     * @param eventData Associated event data.
     * @param sender Sender of the event.
     * @param recipients Recipients of the event.
     */
    public void sendEvent(String eventName, Object eventData, final String sender, final String recipients) {
        getRabbitTemplate().convertAndSend(exchange.getName(), eventName, eventData, new MessagePostProcessor() {
            
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                return decorateMessage(message, sender, recipients);
            }
            
        });
    }
    
    /**
     * Given a Message, supplement the message with additional properties/attributes ( recipients,
     * sender).
     * 
     * @param message The message.
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs.
     * @return The decorated Message.
     */
    private Message decorateMessage(Message message, String sender, String recipients) {
        MessageProperties props = message.getMessageProperties();
        props.setHeader(SENDER_PROPERTY, sender);
        props.setHeader(RECIPIENTS_PROPERTY, recipients);
        return message;
    }
    
}
