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
package org.carewebframework.api.event;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.messaging.Recipient;
import org.carewebframework.api.spring.SpringUtil;

/**
 * Manages local event subscriptions and local and global event delivery.
 */
public class EventManager implements ILocalEventDispatcher, IEventManager {
    
    private static final Log log = LogFactory.getLog(EventManager.class);
    
    private final EventSubscriptions<Object> subscriptions = new EventSubscriptions<>();
    
    private IGlobalEventDispatcher globalEventDispatcher;
    
    /**
     * Returns the event manager for this application context.
     * 
     * @return IEventManager
     */
    public static IEventManager getInstance() {
        return SpringUtil.getBean("eventManager", IEventManager.class);
    }
    
    /**
     * Invoked by IOC container when service is being unloaded. Removes all existing subscriptions.
     */
    public void destroy() {
        subscriptions.clear();
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#hasSubscribers(java.lang.String)
     */
    @Override
    public boolean hasSubscribers(String eventName) {
        return subscriptions.hasSubscribers(eventName);
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#hasSubscribers(java.lang.String)
     */
    @Override
    public boolean hasSubscribers(String eventName, boolean exact) {
        return subscriptions.hasSubscribers(eventName, exact);
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#fireLocalEvent(java.lang.String,
     *      java.lang.Object)
     * @see org.carewebframework.api.event.ILocalEventDispatcher#fireLocalEvent(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void fireLocalEvent(String eventName, Object eventData) {
        // TODO: Handle trace mode here
        subscriptions.invokeCallbacks(eventName, eventData);
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#fireRemoteEvent(java.lang.String,
     *      java.lang.Object, java.lang.String)
     */
    @Override
    public void fireRemoteEvent(String eventName, Object eventData, Recipient... recipients) {
        if (globalEventDispatcher != null) {
            try {
                globalEventDispatcher.fireRemoteEvent(eventName, (Serializable) eventData, recipients);
            } catch (Throwable e) {
                log.error("Error during remote event dispatch.", e);
            }
        }
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#subscribe(java.lang.String,
     *      org.carewebframework.api.event.IGenericEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(String eventName, IGenericEvent<?> subscriber) {
        if (subscriptions.addSubscriber(eventName, (IGenericEvent<Object>) subscriber) == 1) {
            hostSubscribe(eventName, true);
        }
    }
    
    /**
     * @see org.carewebframework.api.event.IEventManager#unsubscribe(java.lang.String,
     *      org.carewebframework.api.event.IGenericEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void unsubscribe(String eventName, IGenericEvent<?> subscriber) {
        if (subscriptions.removeSubscriber(eventName, (IGenericEvent<Object>) subscriber) == 0) {
            hostSubscribe(eventName, false);
        }
    }
    
    /**
     * Synchronizes all existing subscriptions with the global event manager.
     */
    public void hostSynch() {
        for (String eventName : subscriptions.getEvents()) {
            hostSubscribe(eventName, true);
        }
    }
    
    /**
     * Registers or unregisters a subscription with the global event dispatcher, if one is present.
     * 
     * @param eventName Name of event
     * @param subscribe If true, a subscription is registered. If false, it is unregistered.
     */
    private void hostSubscribe(String eventName, boolean subscribe) {
        if (globalEventDispatcher != null) {
            try {
                globalEventDispatcher.subscribeRemoteEvent(eventName, subscribe);
            } catch (Throwable e) {
                log.error(
                    "Error " + (subscribe ? "subscribing to" : "unsubscribing from") + " remote event '" + eventName + "'",
                    e);
            }
        }
    }
    
    /**
     * @see org.carewebframework.api.event.ILocalEventDispatcher#setGlobalEventDispatcher(org.carewebframework.api.event.IGlobalEventDispatcher)
     */
    @Override
    public void setGlobalEventDispatcher(IGlobalEventDispatcher globalEventDispatcher) {
        this.globalEventDispatcher = globalEventDispatcher;
        hostSynch();
    }
    
    /**
     * @see org.carewebframework.api.event.ILocalEventDispatcher#getGlobalEventDispatcher()
     */
    @Override
    public IGlobalEventDispatcher getGlobalEventDispatcher() {
        return globalEventDispatcher;
    }
    
}
