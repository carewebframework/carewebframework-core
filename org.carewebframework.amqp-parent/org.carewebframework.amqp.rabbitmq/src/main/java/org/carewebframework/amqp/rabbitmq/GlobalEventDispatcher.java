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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.event.AbstractGlobalEventDispatcher;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * This class is responsible for communicating with the global messaging server (in this case, an
 * AMQP server). It interacts with the local event manager and is responsible for dispatching
 * (publishing) events to be distributing globally to the messaging server and receiving subscribed
 * events from the same and passing them on to the local event dispatcher for local distribution.
 */
public class GlobalEventDispatcher extends AbstractGlobalEventDispatcher implements MessageListener {
    
    private static final Log log = LogFactory.getLog(GlobalEventDispatcher.class);
    
    private final Map<String, Subscriber> subscribers = Collections.synchronizedMap(new HashMap<String, Subscriber>());
    
    private Broker broker;
    
    /**
     * Create the global event dispatcher.
     */
    public GlobalEventDispatcher() {
        super();
    }
    
    @Override
    protected String getNodeId() {
        return broker.getExchange().getName();
    }
    
    /**
     * Cleanup this instance.
     */
    @Override
    public void destroy() {
        super.destroy();
        removeSubscriptions();
    }
    
    /**
     * Remove all remote subscriptions.
     */
    private void removeSubscriptions() {
        for (final SimpleMessageListenerContainer subscriber : this.subscribers.values()) {
            try {
                subscriber.stop();
            } catch (final Throwable e) {
                log.debug("Error closing subscriber", e);//is level appropriate - previously hidden exception -afranken
            }
        }
        
        this.subscribers.clear();
    }
    
    /**
     * Queue a subscription request.
     * 
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#subscribeRemoteEvent(java.lang.String,
     *      boolean)
     */
    @Override
    public void subscribeRemoteEvent(final String eventName, final boolean subscribe) {
        try {
            if (subscribe) {
                doHostSubscribe(eventName);
            } else {
                doHostUnsubscribe(eventName);
            }
        } catch (final AmqpException e) {
            log.error(e);
        }
    }
    
    /**
     * Registers an event subscription with the global event manager. Note that the global event
     * manager has no knowledge of each event's individual subscribers - only that the event of a
     * given name has subscribers. This is because the global event manager need only dispatch
     * events to the local event manager. The local event manager will then dispatch events to the
     * individual subscribers.
     * 
     * @param eventName Name of event.
     * @throws AmqpException RabbitMQ exception.
     */
    private void doHostSubscribe(final String eventName) throws AmqpException {
        
        if (this.subscribers.get(eventName) != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Already subscribed to Topic[%s]", eventName));
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Subscribing to Topic[%s]", eventName));
        }
        
        broker.declareEventQueue(eventName);
        Subscriber subscriber = new Subscriber(publisherInfo);
        subscriber.setMessageListener(this);
        subscriber.setConnectionFactory(broker.getRabbitTemplate().getConnectionFactory());
        subscriber.setQueueNames(eventName);
        subscriber.start();
        subscribers.put(eventName, subscriber);
    }
    
    /**
     * Removes an event subscription with the global event manager.
     * 
     * @param eventName Name of event
     * @throws AmqpException RabbitMQ exception.
     */
    private void doHostUnsubscribe(final String eventName) throws AmqpException {
        final Subscriber subscriber = this.subscribers.remove(eventName);
        
        if (subscriber != null) {
            log.debug(String.format("Unsubscribing Subscriber[%s] for Topic [%s].", subscriber, eventName));
            subscriber.stop();
        }
    }
    
    /**
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#fireRemoteEvent(java.lang.String,
     *      java.io.Serializable, java.lang.String)
     */
    @Override
    public void fireRemoteEvent(final String eventName, final Serializable eventData, final String recipients) {
        try {
            doFireRemoteEvent(eventName, eventData, recipients);
        } catch (final AmqpException e) {
            log.error("Error firing remote event.", e);
        }
    }
    
    /**
     * Publishes the specified event to the messaging server.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients List of recipients for the event (null or empty string means all
     *            subscribers).
     * @throws AmqpException RabbitMQ exception.
     */
    private void doFireRemoteEvent(final String eventName, final Object eventData, final String recipients)
                                                                                                           throws AmqpException {
        broker.sendEvent(eventName, eventData, publisherInfo.getEndpointId(), recipients);
    }
    
    /**
     * This is the callback for messages received from the AMQP server.
     * 
     * @param message Message received from the AMQP server.
     */
    @Override
    public void onMessage(final Message message) {
        if (log.isDebugEnabled()) {
            log.debug("Message received: " + message);
        }
        
        processMessage(message);
    }
    
    /**
     * Process a dequeued message by forwarding it to the local event manager for local delivery. If
     * the message is a ping request, send the response.
     * 
     * @param message Message to process.
     */
    protected void processMessage(final Message message) {
        try {
            String eventName = message.getMessageProperties().getReceivedRoutingKey();
            Object eventData = broker.getRabbitTemplate().getMessageConverter().fromMessage(message);
            localEventDelivery(eventName, eventData);
        } catch (final Exception e) {
            log.error("Error during local dispatch of global event.", e);
        }
    }
    
    /**
     * Override to do any special setup prior to processing of messages.
     * 
     * @return True if OK to proceed.
     */
    @Override
    protected boolean beginMessageProcessing() {
        return true;
    }
    
    /**
     * Override to do any special teardown after processing of messages.
     */
    @Override
    protected void endMessageProcessing() {
        
    }
    
    /**
     * Sets the AMQP broker.
     * 
     * @param broker The AMQP broker.
     */
    public void setBroker(Broker broker) {
        this.broker = broker;
    }
    
}
