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
public abstract class BeanRegistry<K, V> extends AbstractRegistry<K, V> implements DestructionAwareBeanPostProcessor, Iterable<V> {
    
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
    
    /**
     * Flag to unregister if the managed bean is of the desired type.
     */
    @Override
    public boolean requiresDestruction(Object bean) {
        return clazz.isInstance(bean);
    }
    
}
