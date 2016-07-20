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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.NodeInfo;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.metainfo.ZScript;
import org.zkoss.zk.ui.metainfo.ZScriptInfo;

/**
 * An event listener associated with an invokable action.
 */
public class ActionListener implements EventListener<Event> {
    
    private static final String ATTR_LISTENER = "ActionListener.";
    
    private IAction action;
    
    private final String eventName;
    
    private final String attrName;
    
    private final Component component;
    
    private Object target;
    
    private ActionType type;
    
    private boolean disabled;
    
    /**
     * Adds/removes an action listener to/from a component using the default onClick trigger event.
     * 
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. This may be either the
     *            action script or a registered action name. If empty or null, disassociates the
     *            event listener from the component.
     * @return The newly created action listener.
     */
    public static ActionListener addAction(Component component, String action) {
        return addAction(component, createAction(action));
    }
    
    /**
     * Adds/removes an action listener to/from a component using the default onClick trigger event.
     * 
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. If null, disassociates the
     *            event listener from the component.
     * @return The newly created action listener.
     */
    public static ActionListener addAction(Component component, IAction action) {
        return addAction(component, action, Events.ON_CLICK);
    }
    
    /**
     * Adds/removes an action to/from a component.
     * 
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. This may be either the
     *            action script or a registered action name. If empty or null, disassociates the
     *            event listener from the component.
     * @param eventName The name of the event that will trigger the action.
     * @return The newly created action listener, or null if removed.
     */
    public static ActionListener addAction(Component component, String action, String eventName) {
        return addAction(component, createAction(action), eventName);
    }
    
    /**
     * Adds/removes an action to/from a component.
     * 
     * @param component Component to be associated with the action.
     * @param action Action to invoke when listener event is triggered. If null, disassociates the
     *            event listener from the component.
     * @param eventName The name of the event that will trigger the action.
     * @return The newly created or just removed action listener.
     */
    public static ActionListener addAction(Component component, IAction action, String eventName) {
        ActionListener listener;
        
        if (action == null) {
            listener = removeAction(component, eventName);
        } else {
            listener = getListener(component, eventName);
            
            if (listener == null) {
                listener = new ActionListener(component, action, eventName);
            } else {
                listener.updateAction(action);
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
    public static ActionListener removeAction(Component component) {
        return removeAction(component, Events.ON_CLICK);
    }
    
    /**
     * Removes any action associated with a component.
     * 
     * @param component Component whose action is to be removed.
     * @param eventName The event whose associated action is being removed.
     * @return The removed deferred event listener, or null if none found.
     */
    public static ActionListener removeAction(Component component, String eventName) {
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
    public static void disableAction(Component component, boolean disable) {
        disableAction(component, Events.ON_CLICK, disable);
    }
    
    /**
     * Enables or disables the deferred event listener associated with the component.
     * 
     * @param component The component.
     * @param eventName The name of the event.
     * @param disable Disable state for listener.
     */
    public static void disableAction(Component component, String eventName, boolean disable) {
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
    public static ActionListener getListener(Component component) {
        return getListener(component, Events.ON_CLICK);
    }
    
    /**
     * Returns the listener associated with the given component and event.
     * 
     * @param component The component.
     * @param eventName The event name.
     * @return A DeferredEventListener, or null if not found.
     */
    public static ActionListener getListener(Component component, String eventName) {
        return (ActionListener) component.getAttribute(getAttrName(eventName));
    }
    
    /**
     * Returns the attribute name where the listener reference is stored.
     * 
     * @param eventName The event anem.
     * @return The attribute name.
     */
    private static String getAttrName(String eventName) {
        return ATTR_LISTENER + eventName;
    }
    
    /**
     * Creates an action listener.
     * 
     * @param component Component receiving the event.
     * @param action Action to be invoked upon receipt of the event.
     * @param eventName The name of the event.
     */
    private ActionListener(Component component, IAction action, String eventName) {
        this.component = component;
        this.action = action;
        this.eventName = eventName;
        this.attrName = getAttrName(eventName);
        removeAction(component, eventName);
        component.addEventListener(eventName, this);
        component.setAttribute(attrName, this);
    }
    
    /**
     * Updates the action associated with this listener.
     * 
     * @param action New action value.
     */
    private void updateAction(IAction action) {
        this.action = action;
        this.target = null;
        this.type = null;
    }
    
    /**
     * Remove this listener from its associated component.
     */
    private void removeAction() {
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
    public void onEvent(Event event) throws Exception {
        if (isDisabled()) {
            return;
        }
        
        resolveAction();
        
        if (target != null) {
            switch (type) {
                case URL:
                    Executions.getCurrent().sendRedirect((String) target, "_blank");
                    break;
                    
                case JSCRIPT:
                    Executions.getCurrent().addAuResponse((AuScript) target);
                    break;
                    
                case ZUL:
                case ZSCRIPT:
                    ZScript zscript = (ZScript) target;
                    Page page = component.getPage();
                    page.interpret(zscript.getLanguage(), zscript.getContent(page, component), component);
                    break;
            }
        }
    }
    
    /**
     * Strips the prefix from an action.
     * 
     * @param action The action.
     * @return The action without the prefix.
     */
    private String stripPrefix(String action) {
        return action.substring(action.indexOf(':') + 1);
    }
    
    /**
     * Resolve the action upon first invocation.
     */
    private void resolveAction() {
        if (type == null) {
            String script = action.getScript();
            type = ActionType.getType(script);
            
            switch (type) {
                case ZSCRIPT:
                    target = ZScript.parseContent(stripPrefix(script));
                    break;
                    
                case JSCRIPT:
                    target = new AuScript(stripPrefix(script));
                    break;
                    
                case URL:
                    target = script;
                    break;
                    
                case ZUL:
                    PageDefinition pd = ZKUtil.loadZulPageDefinition(script);
                    
                    for (NodeInfo node : pd.getChildren()) {
                        if (node instanceof ZScriptInfo) {
                            target = ((ZScriptInfo) node).getZScript();
                            break;
                        }
                    }
                    break;
                    
                default:
                    target = null;
            }
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
