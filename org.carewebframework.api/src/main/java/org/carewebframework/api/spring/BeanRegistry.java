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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Base class for deriving registries that track managed beans of a given class or interface type.
 * 
 * @param <T> The class or interface being tracked.
 */
public class BeanRegistry<T> implements DestructionAwareBeanPostProcessor, Iterable<T> {
    
    private final List<T> members = new ArrayList<T>();
    
    private final Class<T> clazz;
    
    /**
     * Create a registry that tracks beans of the given class.
     * 
     * @param clazz
     */
    protected BeanRegistry(Class<T> clazz) {
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
            members.add((T) bean);
        }
        
        return bean;
    }
    
    /**
     * If the managed bean is of the desired type, remove it from the registry.
     */
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (clazz.isInstance(bean)) {
            members.remove(bean);
        }
    }
    
    /**
     * Returns a list of registered beans.
     * 
     * @return
     */
    protected List<T> getMembers() {
        return new ArrayList<T>(members);
    }
    
    @Override
    public Iterator<T> iterator() {
        return members.iterator();
    }
    
}
