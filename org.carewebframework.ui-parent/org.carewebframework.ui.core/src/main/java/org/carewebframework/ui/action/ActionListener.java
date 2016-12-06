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

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

/**
 * An event listener associated with an invokable action.
 */
public class ActionListener implements IEventListener {
    
    private static final String ATTR_LISTENER = "ActionListener.";
    
    private IAction action;
    
    private final String eventName;
    
    private final String attrName;
    
    private final BaseComponent component;
    
    private Object target;
    
    private IActionType actionType;
    
    private boolean disabled;
    
    /**
     * Returns the attribute name where the listener reference is stored.
     * 
     * @param eventName The event name.
     * @return The attribute name.
     */
    protected static String getAttrName(String eventName) {
        return ATTR_LISTENER + eventName;
    }
    
    /**
     * Creates an action listener.
     * 
     * @param component Component receiving the event.
     * @param action Action to be invoked upon receipt of the event.
     * @param eventName The name of the event.
     */
    protected ActionListener(BaseComponent component, IAction action, String eventName) {
        this.component = component;
        this.action = action;
        this.eventName = eventName;
        this.attrName = getAttrName(eventName);
        ActionUtil.removeAction(component, eventName);
        component.addEventListener(eventName, this);
        component.setAttribute(attrName, this);
    }
    
    /**
     * Updates the action associated with this listener.
     * 
     * @param action New action value.
     */
    protected void updateAction(IAction action) {
        this.action = action;
        this.target = null;
        this.actionType = null;
    }
    
    /**
     * Remove this listener from its associated component.
     */
    protected void removeAction() {
        component.removeEventListener(eventName, this);
        
        if (component.getAttribute(attrName) == this) {
            component.removeAttribute(attrName);
        }
    }
    
    /**
     * Listener for the target event. Initial execution resolves the action.
     * 
     * @param event The target event.
     * @throws Exception Unspecified exception.
     */
    @Override
    public void onEvent(Event event) {
        if (isDisabled()) {
            return;
        }
        
        resolveAction();
        actionType.execute(target);
    }
    
    /**
     * Resolve the action upon first invocation.
     */
    private void resolveAction() {
        if (actionType == null) {
            String script = action.getScript();
            actionType = ActionTypeRegistry.getType(script);
            target = actionType.parse(script);
        }
    }
    
    /**
     * Returns true if the listener has been disabled.
     * 
     * @return True if listener is disabled.
     */
    public boolean isDisabled() {
        return disabled || action.isDisabled();
    }
    
    /**
     * Enables or disables the listener.
     * 
     * @param disabled If true, the listener is disabled and any events received are ignored.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
