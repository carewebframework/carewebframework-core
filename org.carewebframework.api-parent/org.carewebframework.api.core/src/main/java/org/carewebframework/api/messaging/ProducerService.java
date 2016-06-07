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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Aggregator for multiple producers.
 */
public class ProducerService implements DestructionAwareBeanPostProcessor {
    
    private final Set<IMessageProducer> producers = new LinkedHashSet<>();
    
    /**
     * @return The list of registered producers.
     */
    public Collection<IMessageProducer> getRegisteredProducers() {
        return Collections.unmodifiableCollection(producers);
    }
    
    /**
     * Registers a producer.
     * 
     * @param producer The producer to register.
     * @return True if not already registered.
     */
    public boolean registerProducer(IMessageProducer producer) {
        return producers.add(producer);
    }
    
    /**
     * Unregisters a producer.
     * 
     * @param producer The producer to unregister.
     * @return False if not already registered.
     */
    public boolean unregisterProducer(IMessageProducer producer) {
        return producers.remove(producer);
    }
    
    /**
     * Publish a message.
     * 
     * @param message Message to publish.
     * @return True if successfully published.
     */
    public boolean publish(Message message) {
        boolean result = false;
        prepare(message);
        
        for (IMessageProducer producer : producers) {
            result |= producer.publish(message);
        }
        
        return result;
    }
    
    /**
     * Publish a message to the producer of the specified class.
     * 
     * @param message Message to publish
     * @param clazz Class of the producer.
     * @return True if successfully published.
     */
    public boolean publish(Message message, Class<? extends IMessageProducer> clazz) {
        IMessageProducer producer = clazz == null ? null : findRegisteredProducer(clazz);
        return publish(message, producer);
    }
    
    /**
     * Publish a message to the producer of the specified class.
     * 
     * @param message Message to publish
     * @param className Fully specified name of the producer's class.
     * @return True if successfully published.
     */
    public boolean publish(Message message, String className) {
        try {
            IMessageProducer producer = findRegisteredProducer(Class.forName(className, false, null));
            return publish(message, producer);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Publish a message to the specified producer. Use this only when publishing to a single
     * producer.
     * 
     * @param message Message to publish.
     * @param producer The message producer.
     * @return True if successfully published.
     */
    private boolean publish(Message message, IMessageProducer producer) {
        if (producer != null) {
            prepare(message);
            return producer.publish(message);
        }
        
        return false;
    }
    
    /**
     * Returns a producer of the specified class.
     * 
     * @param clazz Class of the producer sought.
     * @return The producer, or null if not found.
     */
    private IMessageProducer findRegisteredProducer(Class<?> clazz) {
        for (IMessageProducer producer : producers) {
            if (clazz.isInstance(producer)) {
                return producer;
            }
        }
        
        return null;
    }
    
    /**
     * Adds publication-specific metadata to the message.
     * 
     * @param message The message.
     * @return The original message.
     */
    private Message prepare(Message message) {
        message.setMetadata("cwf-pubid", UUID.randomUUID().toString());
        message.setMetadata("cwf-published", System.currentTimeMillis());
        return message;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IMessageProducer) {
            registerProducer((IMessageProducer) bean);
        }
        
        return bean;
    }
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof IMessageProducer) {
            unregisterProducer((IMessageProducer) bean);
        }
    }
    
}
