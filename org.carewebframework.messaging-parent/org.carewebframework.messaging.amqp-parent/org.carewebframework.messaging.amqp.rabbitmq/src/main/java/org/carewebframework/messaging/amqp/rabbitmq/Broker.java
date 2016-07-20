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
package org.carewebframework.messaging.amqp.rabbitmq;

import org.carewebframework.api.messaging.Message;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

/**
 * AMQP broker administration.
 */
public class Broker {
    
    private final ConnectionFactory connectionFactory;
    
    private final Exchange exchange;
    
    private final RabbitAdmin admin;
    
    /**
     * Creates a broker instance with the specified connection factory and default exchange.
     * 
     * @param connectionFactory Connection factory.
     * @param exchange Default exchange for message delivery.
     */
    public Broker(ConnectionFactory connectionFactory, Exchange exchange) {
        this.connectionFactory = connectionFactory;
        admin = new RabbitAdmin(connectionFactory);
        this.exchange = exchange;
    }
    
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
    
    /**
     * Creates a channel if one does not exist.
     * 
     * @param channel The channel name.
     */
    public void ensureChannel(String channel) {
        if (!channelExists(channel)) {
            createChannel(channel);
        }
    }
    
    /**
     * Returns true if the named channel already exists.
     * 
     * @param channel The channel name.
     * @return True if the channel exists.
     */
    private boolean channelExists(String channel) {
        return admin.getQueueProperties(channel) != null;
    }
    
    /**
     * Creates an event queue (thread safe) with the correct binding.
     * 
     * @param channel Name of event handled by queue.
     */
    private synchronized void createChannel(String channel) {
        if (!channelExists(channel)) {
            Queue queue = new Queue(channel, true, false, true);
            admin.declareQueue(queue);
            Binding binding = new Binding(channel, DestinationType.QUEUE, exchange.getName(), channel + ".#", null);
            admin.declareBinding(binding);
        }
    }
    
    /**
     * Sends an event to the default exchange.
     * 
     * @param channel Name of the channel.
     * @param message Message to send.
     */
    public void sendMessage(String channel, Message message) {
        ensureChannel(channel);
        admin.getRabbitTemplate().convertAndSend(exchange.getName(), channel, message);
    }
    
    public Message convertMessage(org.springframework.amqp.core.Message message) {
        Object msg = admin.getRabbitTemplate().getMessageConverter().fromMessage(message);
        return msg instanceof Message ? (Message) msg : new Message("amqpMessage", msg);
    }
    
}
