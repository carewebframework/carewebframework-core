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
package org.carewebframework.plugin.infopanel.controller;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.plugin.infopanel.model.IActionTarget;
import org.carewebframework.plugin.infopanel.model.IInfoPanel.Action;

/**
 * Listens for a generic event and executes the associated action on the target when the event is
 * detected.
 */
public class ActionListener implements IGenericEvent<Object> {
    
    private final String eventName;
    
    private final Action action;
    
    private IEventManager eventManager;
    
    private final List<IActionTarget> targets = new ArrayList<>();
    
    /**
     * Binds the action listeners to the specified target.
     * 
     * @param target The target to be bound to the created listeners.
     * @param actionListeners The action listeners to be bound.
     */
    public static void bindActionListeners(IActionTarget target, List<ActionListener> actionListeners) {
        if (actionListeners != null) {
            for (ActionListener actionListener : actionListeners) {
                actionListener.bind(target);
            }
        }
    }
    
    /**
     * Unbinds all action listeners from the specified target.
     * 
     * @param target The action target.
     * @param actionListeners List of action listeners.
     */
    public static void unbindActionListeners(IActionTarget target, List<ActionListener> actionListeners) {
        if (actionListeners != null) {
            for (ActionListener listener : actionListeners) {
                listener.unbind(target);
            }
        }
    }
    
    public ActionListener(String eventName, Action action) {
        this.eventName = eventName;
        this.action = action;
    }
    
    /**
     * This is the callback for the generic event that is monitored by this listener. It will result
     * in the invocation of the doAction method of each bound action target.
     */
    @Override
    public void eventCallback(String eventName, Object eventData) {
        for (IActionTarget target : new ArrayList<>(targets)) {
            try {
                target.doAction(action);
            } catch (Throwable t) {
                
            }
        }
    }
    
    /**
     * Binds the specified action target to this event listener.
     * 
     * @param target The action target to bind.
     */
    private void bind(IActionTarget target) {
        if (targets.isEmpty()) {
            getEventManager().subscribe(eventName, this);
        }
        
        targets.add(target);
    }
    
    /**
     * Unbinds the specified action target from this event listener.
     * 
     * @param target The action target to unbind.
     */
    private void unbind(IActionTarget target) {
        if (targets.remove(target) && targets.isEmpty()) {
            getEventManager().unsubscribe(eventName, this);
        }
    }
    
    /**
     * Returns a reference to the event manager.
     * 
     * @return The event manager.
     */
    private IEventManager getEventManager() {
        if (eventManager == null) {
            eventManager = EventManager.getInstance();
        }
        
        return eventManager;
    }
}
