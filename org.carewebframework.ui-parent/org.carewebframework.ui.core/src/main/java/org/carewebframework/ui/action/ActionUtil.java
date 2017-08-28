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
package org.carewebframework.ui.action;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.fujion.component.BaseComponent;
import org.fujion.event.ClickEvent;

/**
 * Static utility class.
 */
public class ActionUtil {
    
    /**
     * Comparator for sorting actions alphabetically by display text.
     */
    public static final Comparator<IAction> comparator = new Comparator<IAction>() {
        
        @Override
        public int compare(IAction a1, IAction a2) {
            return a1.toString().compareToIgnoreCase(a2.toString());
        }
        
    };
    
    /**
     * Creates an action object from fields.
     *
     * @param label Action's label name. May be a label reference (prefixed with an '@' character)
     *            or the label itself.
     * @param script Action's script.
     * @return An action object.
     */
    public static IAction createAction(String label, String script) {
        return new Action(script, label, script);
    }
    
    /**
     * Adds/removes an action listener to/from a component using the default click trigger event.
     *
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. This may be either the
     *            action script or a registered action name. If empty or null, dissociates the event
     *            listener from the component.
     * @return The newly created action listener.
     */
    public static ActionListener addAction(BaseComponent component, String action) {
        return addAction(component, createAction(action));
    }
    
    /**
     * Adds/removes an action listener to/from a component using the default click trigger event.
     *
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. If null, dissociates the
     *            event listener from the component.
     * @return The newly created action listener.
     */
    public static ActionListener addAction(BaseComponent component, IAction action) {
        return addAction(component, action, ClickEvent.TYPE);
    }
    
    /**
     * Adds/removes an action to/from a component.
     *
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. This may be either the
     *            action script or a registered action name. If empty or null, dissociates the event
     *            listener from the component.
     * @param eventName The name of the event that will trigger the action.
     * @return The newly created action listener, or null if removed.
     */
    public static ActionListener addAction(BaseComponent component, String action, String eventName) {
        return addAction(component, createAction(action), eventName);
    }
    
    /**
     * Adds/removes an action to/from a component.
     *
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. If null, dissociates the
     *            event listener from the component.
     * @param eventName The name of the event that will trigger the action.
     * @return The newly created or just removed action listener.
     */
    public static ActionListener addAction(BaseComponent component, IAction action, String eventName) {
        ActionListener listener;
        
        if (action == null) {
            listener = removeAction(component, eventName);
        } else {
            listener = getListener(component, eventName);
            
            if (listener == null) {
                listener = new ActionListener(component, action, eventName);
            } else {
                listener.setAction(action);
            }
        }
        
        return listener;
    }
    
    /**
     * Removes any action associated with a component.
     *
     * @param component Component whose action is to be removed.
     * @return The removed deferred event listener, or null if none found.
     */
    public static ActionListener removeAction(BaseComponent component) {
        return removeAction(component, ClickEvent.TYPE);
    }
    
    /**
     * Removes any action associated with a component.
     *
     * @param component Component whose action is to be removed.
     * @param eventName The event whose associated action is being removed.
     * @return The removed deferred event listener, or null if none found.
     */
    public static ActionListener removeAction(BaseComponent component, String eventName) {
        ActionListener listener = getListener(component, eventName);
        
        if (listener != null) {
            listener.removeAction();
        }
        
        return listener;
    }
    
    /**
     * Enables or disables the deferred event listener associated with the component.
     *
     * @param component The component.
     * @param disable Disable state for listener.
     */
    public static void disableAction(BaseComponent component, boolean disable) {
        disableAction(component, ClickEvent.TYPE, disable);
    }
    
    /**
     * Enables or disables the deferred event listener associated with the component.
     *
     * @param component The component.
     * @param eventName The name of the event.
     * @param disable Disable state for listener.
     */
    public static void disableAction(BaseComponent component, String eventName, boolean disable) {
        ActionListener listener = getListener(component, eventName);
        
        if (listener != null) {
            listener.setDisabled(disable);
        }
    }
    
    /**
     * Returns an IAction object given an action.
     *
     * @param action Action.
     * @return Newly created IAction object, or null if action is null or empty.
     */
    private static IAction createAction(String action) {
        IAction actn = null;
        
        if (!StringUtils.isEmpty(action)) {
            if ((actn = ActionRegistry.getRegisteredAction(action)) == null) {
                actn = ActionUtil.createAction(null, action);
            }
        }
        
        return actn;
    }
    
    /**
     * Returns the listener associated with the given component and the default onClick event.
     *
     * @param component The component.
     * @return A DeferredEventListener, or null if not found.
     */
    public static ActionListener getListener(BaseComponent component) {
        return getListener(component, ClickEvent.TYPE);
    }
    
    /**
     * Returns the listener associated with the given component and event.
     *
     * @param component The component.
     * @param eventName The event name.
     * @return A DeferredEventListener, or null if not found.
     */
    public static ActionListener getListener(BaseComponent component, String eventName) {
        return (ActionListener) component.getAttribute(ActionListener.getAttrName(eventName));
    }
    
    /**
     * Enforce singleton.
     */
    private ActionUtil() {
        super();
    }
    
}
