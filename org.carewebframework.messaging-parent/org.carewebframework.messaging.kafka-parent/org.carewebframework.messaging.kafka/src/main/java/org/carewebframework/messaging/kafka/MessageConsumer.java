/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.carewebframework.api.messaging.IMessageConsumer;
import org.carewebframework.api.messaging.Message;

public class MessageConsumer implements IMessageConsumer {
    
    private static final Log log = LogFactory.getLog(MessageConsumer.class);
    
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
            
            log.debug("Kafka message poller has exited.");
        }
        
    }
    
    private final Consumer<Object, Object> consumer;
    
    private final Set<String> channels = new HashSet<>();
    
    private final long pollingInterval;
    
    private final MessagePoller messagePoller = new MessagePoller();
    
    private IMessageCallback callback;
    
    public MessageConsumer(KafkaService service, long pollingInterval) {
        this.pollingInterval = pollingInterval;
        consumer = service.getConsumer();
    }
    
    @Override
    public void setCallback(IMessageCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public boolean subscribe(String channel) {
        return updateSubscriptions(channels.add(channel));
    }
    
    @Override
    public boolean unsubscribe(String channel) {
        return updateSubscriptions(channels.remove(channel));
    }
    
    public void init() {
        messagePoller.start();
    }
    
    public void destroy() {
        messagePoller.terminate();
    }
    
    private boolean updateSubscriptions(boolean doUpdate) {
        if (doUpdate) {
            consumer.subscribe(channels);
        }
        
        return doUpdate;
    }
    
    private void poll() {
        if (!channels.isEmpty()) {
            ConsumerRecords<Object, Object> records;
            
            synchronized (consumer) {
                records = consumer.poll(0);
                consumer.commitAsync();
            }
            
            for (ConsumerRecord<Object, Object> record : records) {
                Object value = record.value();
                Message message = value instanceof Message ? (Message) value : new Message("kafkaMessage", value);
                
                if (callback != null) {
                    callback.onMessage(record.topic(), message);
                }
            }
        }
    }
    
}
