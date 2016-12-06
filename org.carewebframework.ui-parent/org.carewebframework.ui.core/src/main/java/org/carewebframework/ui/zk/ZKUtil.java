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
package org.carewebframework.ui.zk;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseInputboxComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.SelectEvent;

/**
 * General purpose ZK extensions and convenience methods.
 */
public class ZKUtil {
    
    private static final IEventListener deferredDelivery = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            EventUtil.send(event);
        }
        
    };
    
    /**
     * Possible match modes for hierarchical tree search.
     */
    public enum MatchMode {
        AUTO, // Autodetect index vs label
        INDEX, // By node index.
        CASE_SENSITIVE, // Case sensitive by node label.
        CASE_INSENSITIVE // Case insensitive by node label.
    };
    
    /**
     * Detaches all children from a parent.
     * 
     * @param parent Parent component.
     */
    public static void detachChildren(BaseComponent parent) {
        detachChildren(parent, null);
    }
    
    /**
     * Detaches children from a parent. If exclusions is not null, any exclusions will not be
     * removed.
     * 
     * @param parent Parent component.
     * @param exclusions List of child components to be excluded, or null to detach all children.
     */
    public static void detachChildren(BaseComponent parent, List<? extends BaseComponent> exclusions) {
        detach(parent.getChildren(), exclusions);
    }
    
    /**
     * Detaches a list of components.
     * 
     * @param components List of components to detach.
     */
    public static void detach(List<? extends BaseComponent> components) {
        detach(components, null);
    }
    
    /**
     * Detaches a list of components. If exclusions is not null, any exclusions will not be removed.
     * 
     * @param components List of components to detach.
     * @param exclusions List of components to be excluded, or null to detach all.
     */
    public static void detach(List<? extends BaseComponent> components, List<? extends BaseComponent> exclusions) {
        for (int i = components.size() - 1; i >= 0; i--) {
            BaseComponent comp = components.get(i);
            
            if (exclusions == null || !exclusions.contains(comp)) {
                comp.destroy();
            }
        }
    }
    
    /**
     * Recurses over all children of specified component and enables or disables them.
     * 
     * @param parent Parent whose children's disable status is to be modified.
     * @param disable The disable status for the children.
     */
    public static void disableChildren(BaseComponent parent, boolean disable) {
        List<BaseComponent> children = parent.getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            BaseComponent comp = children.get(i);
            
            if (comp instanceof BaseUIComponent) {
                ((BaseUIComponent) comp).setDisabled(disable);
            }
            
            disableChildren(comp, disable);
        }
    }
    
    /**
     * Adds or removes a badge to/from an HTML element.
     * 
     * @param selector Selector for the target element.
     * @param label Text for the badge. Null value removes any existing badge.
     * @param classes Optional additional CSS classes for badge.
     */
    public static void setBadge(String selector, String label, String classes) {
        ClientUtil.invoke("cwf.setBadge", new Object[] { selector, label, classes });
    }
    
    /**
     * Fires an event, deferring delivery if the desktop of the target is not currently active.
     * 
     * @param event The event to fire.
     */
    public static void fireEvent(Event event) {
        fireEvent(event, deferredDelivery);
    }
    
    /**
     * Fires an event to the specified listener, deferring delivery if the page of the target is not
     * currently active.
     * 
     * @param event The event to fire.
     * @param listener The listener to receive the event.
     */
    public static void fireEvent(Event event, IEventListener listener) {
        Page page = event.getTarget() == null ? null : event.getTarget().getPage();
        
        if (page != null && page != ExecutionContext.getPage()) {
            page.getEventQueue().queue(event);
        } else {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }
    
    /**
     * Returns the node associated with the specified \-delimited path.
     * 
     * @param <ANCHOR> Class of the anchor component.
     * @param <NODE> Class of the node component.
     * @param root Root component of hierarchy.
     * @param anchorClass Class of the anchor component.
     * @param nodeClass Class of the node component.
     * @param path \-delimited path to search.
     * @param create If true, nodes are created as needed.
     * @param matchMode The match mode.
     * @return The node corresponding to the specified path, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <ANCHOR extends BaseComponent, NODE extends BaseComponent> NODE findNode(BaseComponent root,
                                                                                           Class<ANCHOR> anchorClass,
                                                                                           Class<NODE> nodeClass,
                                                                                           String path, boolean create,
                                                                                           MatchMode matchMode) {
        String[] pcs = path.split("\\\\");
        BaseComponent node = null;
        
        try {
            for (String pc : pcs) {
                if (pc.isEmpty()) {
                    continue;
                }
                
                BaseComponent parent = node == null ? root : node.getChild(anchorClass);
                
                if (parent == null) {
                    if (!create) {
                        return null;
                    }
                    node.addChild(parent = anchorClass.newInstance());
                }
                
                node = null;
                int index = matchMode == MatchMode.INDEX || matchMode == MatchMode.AUTO ? NumberUtils.toInt(pc, -1) : -1;
                MatchMode mode = matchMode != MatchMode.AUTO ? matchMode
                        : index >= 0 ? MatchMode.INDEX : MatchMode.CASE_INSENSITIVE;
                List<BaseComponent> children = parent.getChildren();
                int size = children.size();
                
                if (mode == MatchMode.INDEX) {
                    
                    if (index < 0) {
                        index = size;
                    }
                    
                    int deficit = index - size;
                    
                    if (!create && deficit >= 0) {
                        return null;
                    }
                    
                    while (deficit-- >= 0) {
                        parent.addChild(nodeClass.newInstance());
                    }
                    node = children.get(index);
                    
                } else {
                    for (BaseComponent child : children) {
                        String label = BeanUtils.getProperty(child, "label");
                        
                        if (mode == MatchMode.CASE_SENSITIVE ? pc.equals(label) : pc.equalsIgnoreCase(label)) {
                            node = child;
                            break;
                        }
                    }
                    
                    if (node == null) {
                        if (!create) {
                            return null;
                        }
                        node = nodeClass.newInstance();
                        parent.addChild(node);
                        BeanUtils.setProperty(node, "label", pc);
                    }
                }
                
                if (node == null) {
                    break;
                }
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        return (NODE) node;
    }
    
    /**
     * Converts a JavaScript snippet to serializable form. If the snippet does not have a function
     * wrapper, a no-argument wrapper will be added.
     * 
     * @param snippet JS code snippet.
     * @return A JavaScriptValue object or null if the input was null.
     */
    public static String toJavaScriptValue(String snippet) {
        return snippet == null ? null : snippet.startsWith("function") ? snippet : "function() {" + snippet + "}";
    }
    
    /**
     * Recursively wires input elements to a common event handler for the detection of changes.
     * 
     * @param parent The parent component.
     * @param targetComponent The component to receive the change events.
     * @param targetEvent The name of the event to send to the target component.
     */
    public static void wireChangeEvents(BaseComponent parent, BaseComponent targetComponent, String targetEvent) {
        for (BaseComponent child : parent.getChildren()) {
            String sourceEvents = null;
            
            if (child instanceof Combobox) {
                sourceEvents = ChangeEvent.TYPE + "," + SelectEvent.TYPE;
            } else if (child instanceof BaseInputboxComponent) {
                sourceEvents = ChangeEvent.TYPE;
            } else if (child instanceof Checkbox) {
                sourceEvents = ChangeEvent.TYPE;
            }
            
            if (sourceEvents != null) {
                for (String eventName : sourceEvents.split("\\,")) {
                    child.registerEventListener(eventName, (event) -> {
                        event = new Event(targetEvent, targetComponent);
                        EventUtil.post(event);
                    });
                }
            }
            
            wireChangeEvents(child, targetComponent, targetEvent);
        }
        
    }
    
    /**
     * Enforces static class.
     */
    private ZKUtil() {
    }
}
