/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.messaging.kafka;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.carewebframework.api.event.AbstractGlobalEventDispatcher;
import org.carewebframework.api.thread.ThreadUtil;

/**
 */
public class GlobalEventDispatcher extends AbstractGlobalEventDispatcher {
    
    private static final Log log = LogFactory.getLog(GlobalEventDispatcher.class);
    
    private class MessagePoller extends Thread {
        
        private final Object monitor = new Object();
        
        private boolean terminate;
        
        /**
         * Wakes up the background thread.
         *
         * @return True if request was successful.
         */
        public synchronized boolean wakeup() {
            try {
                synchronized (monitor) {
                    monitor.notify();
                }
                return true;
            } catch (Throwable t) {
                return false;
            }
        }
        
        public void terminate() {
            terminate = true;
            wakeup();
        }
        
        @Override
        public void run() {
            synchronized (monitor) {
                while (!terminate) {
                    try {
                        poll();
                        monitor.wait(pollingInterval);
                    } catch (InterruptedException e) {}
                }
            }
            
            log.debug("Message poller has exited.");
        }
        
    }
    
    private final Consumer<Object, Message> consumer;
    
    private final Producer<Object, Message> producer;
    
    private final Set<String> topics = new HashSet<>();
    
    private final long pollingInterval = 1000;
    
    private MessagePoller messagePoller;
    
    /**
     * Create the global event dispatcher.
     * 
     * @param service The Kafka service.
     */
    public GlobalEventDispatcher(KafkaService service) {
        super();
        this.producer = service.getProducer();
        this.consumer = service.getNewConsumer();
    }
    
    @Override
    protected void updateConnectionStatus(boolean connected) {
        publisherInfo.setNodeId(getNodeId());
        super.updateConnectionStatus(connected);
    }
    
    @Override
    protected String getNodeId() {
        return null;
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    @Override
    public void init() {
        super.init();
    }
    
    /**
     * Cleanup this instance.
     */
    @Override
    public void destroy() {
        super.destroy();
        
        if (messagePoller != null) {
            messagePoller.terminate();
        }
        
        removeSubscriptions();
        consumer.close();
    }
    
    /**
     * Remove all remote subscriptions.
     */
    private void removeSubscriptions() {
        topics.clear();
        updateSubscriptions();
    }
    
    /**
     * Process a subscription request.
     * 
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#subscribeRemoteEvent(java.lang.String,
     *      boolean)
     */
    @Override
    public void subscribeRemoteEvent(String eventName, boolean subscribe) {
        if (subscribe) {
            doHostSubscribe(eventName);
        } else {
            doHostUnsubscribe(eventName);
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
     */
    private void doHostSubscribe(String eventName) {
        String topic = getTopicName(eventName);
        
        if (topics.add(topic)) {
            updateSubscriptions();
        }
    }
    
    /**
     * Extracts the topic name from an event name.
     * 
     * @param eventName Event name.
     * @return Topic name (highest level of event hierarchy).
     */
    private String getTopicName(String eventName) {
        int i = eventName.indexOf('.');
        return i < 0 ? eventName : eventName.substring(0, i);
    }
    
    /**
     * Removes an event subscription with the global event manager.
     * 
     * @param eventName Name of event
     */
    private void doHostUnsubscribe(String eventName) {
        String topic = getTopicName(eventName);
        
        if (topics.remove(topic)) {
            updateSubscriptions();
        }
    }
    
    private void updateSubscriptions() {
        synchronized (consumer) {
            consumer.subscribe(topics);
            
            if (messagePoller == null) {
                messagePoller = new MessagePoller();
                ThreadUtil.startThread(messagePoller);
            }
        }
    }
    
    private void poll() {
        if (!topics.isEmpty()) {
            ConsumerRecords<Object, Message> records;
            
            synchronized (consumer) {
                records = consumer.poll(0);
                consumer.commitAsync();
            }
            
            for (ConsumerRecord<Object, Message> record : records) {
                processMessage(record.value());
            }
        }
    }
    
    /**
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#fireRemoteEvent(java.lang.String,
     *      java.io.Serializable, java.lang.String)
     */
    @Override
    public void fireRemoteEvent(String eventName, Serializable eventData, String recipients) {
        try {
            doFireRemoteEvent(eventName, eventData, recipients);
        } catch (Exception e) {
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
     * @throws Exception Unspecified exception.
     */
    private void doFireRemoteEvent(String eventName, Serializable eventData, String recipients) throws Exception {
        Message message = new Message(eventName, eventData);
        producer.send(new ProducerRecord<Object, Message>(getTopicName(eventName), message));
    }
    
    /**
     * Process a dequeued message by forwarding it to the local event manager for local delivery. If
     * the message is a ping request, send the response.
     * 
     * @param message Message to process.
     */
    protected void processMessage(Message message) {
        localEventDelivery(message.eventName, message.eventData);
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
    
}
