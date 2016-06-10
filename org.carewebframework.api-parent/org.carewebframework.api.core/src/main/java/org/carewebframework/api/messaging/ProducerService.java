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
    
    private final String nodeId = UUID.randomUUID().toString();
    
    /**
     * @return The unique node id for this service.
     */
    public String getNodeId() {
        return nodeId;
    }
    
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
     * @param channel The channel on which to publish the message.
     * @param message Message to publish.
     * @param recipients Optional list of targeted recipients.
     * @return True if successfully published.
     */
    public boolean publish(String channel, Message message, Recipient... recipients) {
        boolean result = false;
        prepare(channel, message, recipients);
        
        for (IMessageProducer producer : producers) {
            result |= producer.publish(channel, message);
        }
        
        return result;
    }
    
    /**
     * Publish a message to the producer of the specified class.
     * 
     * @param channel The channel on which to publish the message.
     * @param message Message to publish
     * @param clazz Class of the producer.
     * @param recipients Optional list of targeted recipients.
     * @return True if successfully published.
     */
    public boolean publish(String channel, Message message, Class<? extends IMessageProducer> clazz,
                           Recipient... recipients) {
        IMessageProducer producer = clazz == null ? null : findRegisteredProducer(clazz);
        return publish(channel, message, producer, recipients);
    }
    
    /**
     * Publish a message to the producer of the specified class.
     * 
     * @param channel The channel on which to publish the message.
     * @param message Message to publish
     * @param className Fully specified name of the producer's class.
     * @param recipients Optional list of targeted recipients.
     * @return True if successfully published.
     */
    public boolean publish(String channel, Message message, String className, Recipient... recipients) {
        try {
            IMessageProducer producer = findRegisteredProducer(Class.forName(className, false, null));
            return publish(channel, message, producer, recipients);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Publish a message to the specified producer. Use this only when publishing to a single
     * producer.
     * 
     * @param channel The channel on which to publish the message.
     * @param message Message to publish.
     * @param producer The message producer.
     * @param recipients Optional list of targeted recipients.
     * @return True if successfully published.
     */
    private boolean publish(String channel, Message message, IMessageProducer producer, Recipient[] recipients) {
        if (producer != null) {
            prepare(channel, message, recipients);
            return producer.publish(channel, message);
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
    private Message prepare(String channel, Message message, Recipient[] recipients) {
        message.setMetadata("cwf.pub.node", nodeId);
        message.setMetadata("cwf.pub.channel", channel);
        message.setMetadata("cwf.pub.event", UUID.randomUUID().toString());
        message.setMetadata("cwf.pub.when", System.currentTimeMillis());
        message.setMetadata("cwf.pub.recipients", recipients);
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
    
    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof IMessageProducer;
    }
    
}
