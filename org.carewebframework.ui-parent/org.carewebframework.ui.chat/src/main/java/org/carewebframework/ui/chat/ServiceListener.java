/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.chat;

import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;

/**
 * Simple listener implementation that tracks its own subscription.
 * 
 * @param <T>
 */
public abstract class ServiceListener<T> implements IGenericEvent<T> {
    
    private final String eventName;
    
    private final IEventManager eventManager;
    
    ServiceListener(String eventName, IEventManager eventManager) {
        this.eventName = eventName;
        this.eventManager = eventManager;
    }
    
    void setActive(boolean active) {
        if (active) {
            eventManager.subscribe(eventName, this);
        } else {
            eventManager.unsubscribe(eventName, this);
        }
    }
}
