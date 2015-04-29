/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages event subscriptions in a thread-safe way.
 * 
 * @param <T> Event data type.
 */
/*package*/class EventSubscriptions<T> {
    
    private static final Log log = LogFactory.getLog(EventSubscriptions.class);
    
    private final Map<String, List<IGenericEvent<T>>> subscriptions = new HashMap<String, List<IGenericEvent<T>>>();
    
    /**
     * Adds a subscriber to the specified event.
     * 
     * @param eventName Name of the event.
     * @param subscriber Subscriber to add.
     * @return Count of subscribers after the operation.
     */
    public synchronized int addSubscriber(String eventName, IGenericEvent<T> subscriber) {
        List<IGenericEvent<T>> subscribers = getSubscribers(eventName, true);
        subscribers.add(subscriber);
        return subscribers.size();
    }
    
    /**
     * Removes a subscriber from the specified event.
     * 
     * @param eventName Name of the event.
     * @param subscriber Subscriber to remove.
     * @return Count of subscribers after the operation, or -1 no subscriber list existed.
     */
    public synchronized int removeSubscriber(String eventName, IGenericEvent<T> subscriber) {
        List<IGenericEvent<T>> subscribers = getSubscribers(eventName, false);
        
        if (subscribers != null) {
            subscribers.remove(subscriber);
            
            if (subscribers.isEmpty()) {
                subscriptions.remove(eventName);
            }
            
            return subscribers.size();
        }
        
        return -1;
    }
    
    /**
     * Returns true if the event has any subscribers.
     * 
     * @param eventName Name of the event.
     * @return True if subscribers exist.
     */
    public synchronized boolean hasSubscribers(String eventName) {
        return getSubscribers(eventName, false) != null;
    }
    
    /**
     * Returns true If the event has subscribers.
     * 
     * @param eventName Name of the event.
     * @param exact If false, will iterate through parent events until a subscriber is found. If
     *            true, only the exact event is considered.
     * @return True if a subscriber was found.
     */
    public boolean hasSubscribers(String eventName, boolean exact) {
        while (!StringUtils.isEmpty(eventName)) {
            if (hasSubscribers(eventName)) {
                return true;
            } else if (exact) {
                return false;
            } else {
                eventName = stripLevel(eventName);
            }
        }
        
        return false;
    }
    
    /**
     * Returns a thread-safe iterable for the subscriber list.
     * 
     * @param eventName Name of the event.
     * @return Iterable for the subscriber list, or null if no list exists.
     */
    public synchronized Iterable<IGenericEvent<T>> getSubscribers(String eventName) {
        List<IGenericEvent<T>> subscribers = getSubscribers(eventName, false);
        return subscribers == null ? null : new ArrayList<IGenericEvent<T>>(subscribers);
    }
    
    /**
     * Returns a thread-safe iterable for all events with subscribers.
     * 
     * @return List of events.
     */
    public synchronized Iterable<String> getEvents() {
        return new ArrayList<String>(subscriptions.keySet());
    }
    
    /**
     * Removes all subscriptions.
     */
    public synchronized void clear() {
        subscriptions.clear();
    }
    
    /**
     * Invokes callbacks on all subscribers of this and parent events.
     * 
     * @param eventName Name of the event.
     * @param eventData The associated event data.
     */
    public void invokeCallbacks(String eventName, T eventData) {
        String name = eventName;
        
        while (!StringUtils.isEmpty(name)) {
            Iterable<IGenericEvent<T>> subscribers = getSubscribers(name);
            
            if (subscribers != null) {
                for (IGenericEvent<T> subscriber : subscribers) {
                    try {
                        if(log.isDebugEnabled()){
                            log.debug(String.format("Firing local Event[name=%s,data=%s]",eventName, eventData));
                        }
                        subscriber.eventCallback(eventName, eventData);
                    } catch (Throwable e) {
                        log.error("Error during local event callback.", e);
                    }
                }
            }
            
            name = stripLevel(name);
        }
    }
    
    /**
     * Gets the list of subscribers associated with an event.
     * 
     * @param eventName Name of the event.
     * @param canCreate If true and the list does not exist, create it.
     * @return The requested list; may be null.
     */
    private List<IGenericEvent<T>> getSubscribers(String eventName, boolean canCreate) {
        List<IGenericEvent<T>> subscribers = subscriptions.get(eventName);
        
        if (subscribers == null && canCreate) {
            subscribers = new LinkedList<IGenericEvent<T>>();
            subscriptions.put(eventName, subscribers);
        }
        
        return subscribers;
    }
    
    /**
     * Strips the lowest hierarchical level from the event type.
     * 
     * @param eventName Event type.
     * @return Event type with the lowest level removed.
     */
    private String stripLevel(String eventName) {
        int i = eventName.lastIndexOf('.');
        return i > 1 ? eventName.substring(0, i) : "";
    }
    
}
