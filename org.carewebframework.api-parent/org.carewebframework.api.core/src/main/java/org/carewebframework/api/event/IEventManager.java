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

/**
 * Defines the interface for accessing event management services.
 */
public interface IEventManager {
    
    /**
     * Returns true if the specified event has any local subscribers.
     * 
     * @param eventName Name of the event.
     * @return True if the event has subscribers. Includes subscribers to any parent events.
     */
    boolean hasSubscribers(String eventName);
    
    /**
     * Returns true if the specified event has any local subscribers.
     * 
     * @param eventName Name of the event.
     * @param exact If true, do not consider subscribers to parent events.
     * @return True if the event has subscribers.
     */
    boolean hasSubscribers(String eventName, boolean exact);
    
    /**
     * Fires the specified event locally.
     * 
     * @param eventName Name of the event to fire.
     * @param eventData Associated data object.
     */
    void fireLocalEvent(String eventName, Object eventData);
    
    /**
     * Fires the event remotely via the global event manager.
     * 
     * @param eventName Name of the event to fire.
     * @param eventData Associated data object.
     */
    void fireRemoteEvent(String eventName, Object eventData);
    
    /**
     * Fires the event remotely via the global event manager to a specified list of recipients.
     * 
     * @param eventName Name of the event to fire.
     * @param eventData Associated data object.
     * @param recipients Optional recipient list. This is a comma-delimited list of client
     *            identifiers as assigned by the underlying JMS implementation. If null or empty,
     *            the event is delivered to all subscribers. If a specified recipient is not a
     *            subscriber, the event delivery request will be ignored.
     */
    void fireRemoteEvent(String eventName, Object eventData, String recipients);
    
    /**
     * Register an event subscription.
     * 
     * @param eventName Name of event.
     * @param subscriber Subscriber to event.
     */
    void subscribe(String eventName, IGenericEvent<?> subscriber);
    
    /**
     * Unregister an event subscription. If the subscriber has no existing subscription then this
     * call has no effect.
     * 
     * @param eventName Name of event.
     * @param subscriber Subscriber to event.
     */
    void unsubscribe(String eventName, IGenericEvent<?> subscriber);
    
}
