/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.WeakList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 * Core class for managing the application framework. It provides a registration service for
 * components. It also interfaces with the Spring application context to perform automatic bean
 * registration.
 */
public class AppFramework implements ApplicationContextAware, DestructionAwareBeanPostProcessor {
    
    private ApplicationContext appContext;
    
    private final List<Object> registeredObjects = new WeakList<>();
    
    private final List<IRegisterEvent> onRegisterList = new WeakList<>();
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    /**
     * Returns the active application context.
     * 
     * @return The active application context.
     */
    public ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    /**
     * ApplicationContextAware interface to allow container to inject itself. Sets the active
     * application context.
     * 
     * @param appContext The active application context.
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        if (this.appContext != null) {
            throw new ApplicationContextException("Attempt to reinitialize application context.");
        }
        
        this.appContext = appContext;
    }
    
    /**
     * Returns the attribute map.
     * 
     * @return The attribute map.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    /**
     * Gets an attribute value.
     * 
     * @param key Key associated with the requested attribute.
     * @return The attribute value, or null if not found.
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Sets or removes an attribute value.
     * 
     * @param key Key associated with the attribute.
     * @param value Value to assign to the attribute. If null, the specified attribute is removed if
     *            it exists.
     */
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }
    
    /**
     * Registers an object with the framework and relevant subsystems (e.g., the context manager).
     * 
     * @param object Object to register
     * @return True if object successfully registered. False if object was already registered.
     */
    public synchronized boolean registerObject(Object object) {
        if (!MiscUtil.containsInstance(registeredObjects, object)) {
            if (object instanceof IRegisterEvent) {
                IRegisterEvent onRegister = (IRegisterEvent) object;
                onRegisterList.add(onRegister);
                
                for (Object obj : registeredObjects) {
                    onRegister.registerObject(obj);
                }
            }
            
            registeredObjects.add(object);
            
            for (IRegisterEvent onRegister : onRegisterList) {
                onRegister.registerObject(object);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove an object registration from the framework and any relevant subsystems.
     * 
     * @param object Object to unregister.
     * @return True if object successfully unregistered.
     */
    public synchronized boolean unregisterObject(Object object) {
        int i = MiscUtil.indexOfInstance(registeredObjects, object);
        
        if (i > -1) {
            registeredObjects.remove(i);
            
            for (IRegisterEvent onRegister : onRegisterList) {
                onRegister.unregisterObject(object);
            }
            
            if (object instanceof IRegisterEvent) {
                onRegisterList.remove(object);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Finds a registered object belonging to the specified class.
     * 
     * @param clazz Returned object must be assignment-compatible with this class.
     * @param previousInstance Previous instance returned by this call. Search will start with the
     *            entry that follows this one in the list. If this parameter is null, the search
     *            starts at the beginning.
     * @return Reference to the discovered object or null if none found.
     */
    public synchronized Object findObject(Class<?> clazz, Object previousInstance) {
        int i = previousInstance == null ? -1 : MiscUtil.indexOfInstance(registeredObjects, previousInstance);
        
        for (i++; i < registeredObjects.size(); i++) {
            Object object = registeredObjects.get(i);
            
            if (clazz.isInstance(object)) {
                return object;
            }
        }
        
        return null;
    }
    
    /**
     * Automatically registers any container-managed bean with the framework.
     * 
     * @param bean Object to register.
     * @param beanName Name of the managed bean.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        registerObject(bean);
        return bean;
    }
    
    /**
     * Does nothing but return the original bean instance.
     * 
     * @param bean Bean instance.
     * @param beanName Name of the managed bean.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    /**
     * Unregister container-managed bean upon destruction.
     * 
     * @param bean Bean instance.
     * @param beanName Name of the managed bean.
     */
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        unregisterObject(bean);
    }
    
}
