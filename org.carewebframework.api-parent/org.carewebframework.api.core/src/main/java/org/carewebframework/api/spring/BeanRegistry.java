/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.RegistryMap.DuplicateAction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Base class for deriving registries that track managed beans of a given class or interface type.
 * 
 * @param <K> A unique key.
 * @param <V> The class or interface being tracked.
 */
public abstract class BeanRegistry<K, V> extends AbstractRegistry<K, V>implements DestructionAwareBeanPostProcessor, Iterable<V> {
    
    private final Class<V> clazz;
    
    /**
     * Create a registry that tracks beans of the given class.
     * 
     * @param clazz Class of beans to track.
     */
    protected BeanRegistry(Class<V> clazz) {
        super(DuplicateAction.ERROR);
        this.clazz = clazz;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    /**
     * If the managed bean is of the desired type, add it to the registry.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (clazz.isInstance(bean)) {
            register((V) bean);
        }
        
        return bean;
    }
    
    /**
     * If the managed bean is of the desired type, remove it from the registry.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (clazz.isInstance(bean)) {
            unregister((V) bean);
        }
    }
    
}
