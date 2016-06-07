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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.carewebframework.api.messaging.IMessageConsumer.IMessageCallback;
import org.carewebframework.common.DateUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Aggregator for multiple consumers.
 */
public class ConsumerService implements IMessageCallback, DestructionAwareBeanPostProcessor {
    
    private final Set<IMessageConsumer> consumers = new LinkedHashSet<>();
    
    private final Map<String, LinkedHashSet<IMessageCallback>> callbacks = new LinkedHashMap<>();
    
    private final Map<String, Long> delivered = new LinkedHashMap<>();
    
    private final long maxLife;
    
    private long oldest;
    
    /**
     * @param maxLife Maximum life (in milliseconds) of an entry in the message delivery cache.
     */
    public ConsumerService(String maxLife) {
        this.maxLife = (long) DateUtil.parseElapsed(maxLife);
    }
    
    /**
     * @return A list of registered consumers.
     */
    public Collection<IMessageConsumer> getRegisteredConsumers() {
        return Collections.unmodifiableCollection(consumers);
    }
    
    /**
     * Register a message consumer.
     * 
     * @param consumer The consumer to register.
     * @return True if the consumer was not already registered.
     */
    public synchronized boolean registerConsumer(IMessageConsumer consumer) {
        consumer.setCallback(this);
        return consumers.add(consumer);
    }
    
    /**
     * Unregister a message consumer.
     * 
     * @param consumer The consumer to unregister.
     * @return False if the consumer was not already registered.
     */
    public synchronized boolean unregisterConsumer(IMessageConsumer consumer) {
        consumer.setCallback(null);
        return consumers.remove(consumer);
    }
    
    /**
     * Return the callbacks associated with the specified channel.
     * 
     * @param channel The channel.
     * @param autoCreate Create the callback list if one doesn't exist.
     * @param clone Return a clone of the callback list.
     * @return The callback list (possibly null).
     */
    private synchronized LinkedHashSet<IMessageCallback> getCallbacks(String channel, boolean autoCreate, boolean clone) {
        LinkedHashSet<IMessageCallback> result = callbacks.get(channel);
        
        if (result == null && autoCreate) {
            callbacks.put(channel, result = new LinkedHashSet<>());
        }
        
        return result == null ? null : clone ? new LinkedHashSet<>(result) : result;
    }
    
    public synchronized void subscribe(String channel, IMessageCallback callback) {
        boolean newSubscription = !callbacks.containsKey(channel);
        getCallbacks(channel, true, false).add(callback);
        
        if (newSubscription) {
            for (IMessageConsumer consumer : consumers) {
                consumer.subscribe(channel);
            }
        }
    }
    
    public synchronized void unsubscribe(String channel, IMessageCallback callback) {
        LinkedHashSet<IMessageCallback> cbs = getCallbacks(channel, false, false);
        
        if (cbs != null && cbs.remove(callback) && cbs.isEmpty()) {
            callbacks.remove(channel);
            
            for (IMessageConsumer consumer : consumers) {
                consumer.unsubscribe(channel);
            }
        }
    }
    
    @Override
    public void onMessage(Message message) {
        if (updateDelivered(message)) {
            LinkedHashSet<IMessageCallback> callbacks = getCallbacks(message.getChannel(), false, true);
            
            if (callbacks != null) {
                dispatchMessages(message, callbacks);
            }
        }
    }
    
    /**
     * Updates the delivered message cache. This avoids delivering the same message transported by
     * different messaging frameworks. If we have only one messaging framework registered, we don't
     * need to worry about this.
     * 
     * @param message The message being delivered.
     * @return True if the cache was updated (i.e., the message has not been previously delivered).
     */
    private boolean updateDelivered(Message message) {
        if (consumers.size() <= 1) {
            return true;
        }
        
        synchronized (delivered) {
            long now = System.currentTimeMillis();
            long expiry = now - maxLife;
            
            if (oldest <= expiry) {
                Iterator<Entry<String, Long>> iter = delivered.entrySet().iterator();
                
                while (iter.hasNext()) {
                    Entry<String, Long> entry = iter.next();
                    
                    if (entry.getValue() > expiry) {
                        oldest = expiry;
                        break;
                    }
                    
                    iter.remove();
                }
            }
            
            String pubid = (String) message.getMetadata("cwf-pubid");
            boolean result = !delivered.containsKey(pubid);
            
            if (result) {
                if (pubid != null && !pubid.isEmpty()) {
                    delivered.put(pubid, now);
                }
                oldest = delivered.size() == 1 ? now : oldest;
            }
            
            return result;
        }
    }
    
    /**
     * Dispatch message to callback. Override to address special threading considerations.
     * 
     * @param message The message to dispatch.
     * @param callbacks The callbacks to receive the message.
     */
    protected void dispatchMessages(Message message, Set<IMessageCallback> callbacks) {
        for (IMessageCallback callback : callbacks) {
            try {
                callback.onMessage(message);
            } catch (Exception e) {
                
            }
        }
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IMessageConsumer) {
            registerConsumer((IMessageConsumer) bean);
        }
        
        return bean;
    }
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof IMessageConsumer) {
            unregisterConsumer((IMessageConsumer) bean);
        }
    }
    
}
