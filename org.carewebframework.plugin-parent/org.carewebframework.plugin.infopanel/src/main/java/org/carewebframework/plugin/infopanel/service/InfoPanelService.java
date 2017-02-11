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
package org.carewebframework.plugin.infopanel.service;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.plugin.infopanel.controller.ActionListener;
import org.carewebframework.plugin.infopanel.model.IInfoPanel;
import org.carewebframework.plugin.infopanel.model.IInfoPanel.Action;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.web.component.BaseComponent;

/**
 * Static methods for interacting with an info panel.
 */
public class InfoPanelService {
    
    private static final String EVENT_LISTENER_ATTR = "@infopanel.listener";
    
    /**
     * Finds the "nearest" info panel.
     * 
     * @param container The container from which to begin the search.
     * @param activeOnly If true, only active info panels are considered.
     * @return The nearest active info panel, or null if none found.
     */
    public static IInfoPanel findInfoPanel(PluginContainer container, boolean activeOnly) {
        return findInfoPanel(UIElementBase.getAssociatedUIElement(container), activeOnly);
    }
    
    /**
     * Finds the "nearest" active info panel.
     * 
     * @param container The container from which to begin the search.
     * @return The nearest active info panel, or null if none found.
     */
    public static IInfoPanel findInfoPanel(PluginContainer container) {
        return findInfoPanel(container, true);
    }
    
    /**
     * Finds the "nearest" info panel.
     * 
     * @param element The UI element from which to begin the search.
     * @param activeOnly If true, only active info panels are considered.
     * @return The nearest active info panel, or null if none found.
     */
    public static IInfoPanel findInfoPanel(UIElementBase element, boolean activeOnly) {
        UIElementBase parent = element;
        UIElementBase previousParent;
        IInfoPanel infoPanel = searchChildren(element, null, activeOnly);
        
        while ((infoPanel == null) && (parent != null)) {
            previousParent = parent;
            parent = parent.getParent();
            infoPanel = searchChildren(parent, previousParent, activeOnly);
        }
        
        return infoPanel;
    }
    
    /**
     * Finds the "nearest" active info panel.
     * 
     * @param element The UI element from which to begin the search.
     * @return The nearest active info panel, or null if none found.
     */
    public static IInfoPanel findInfoPanel(UIElementBase element) {
        return findInfoPanel(element, true);
    }
    
    /**
     * Search children of the specified parent for an occurrence of an active info panel. This is a
     * recursive, breadth-first search of the component tree.
     * 
     * @param parent Parent whose children are to be searched.
     * @param exclude An optional child to be excluded from the search.
     * @param activeOnly If true, only active info panels are considered.
     * @return The requested info panel, or null if none found.
     */
    private static IInfoPanel searchChildren(UIElementBase parent, UIElementBase exclude, boolean activeOnly) {
        IInfoPanel infoPanel = null;
        
        if (parent != null) {
            for (UIElementBase child : parent.getChildren()) {
                if ((child != exclude) && ((infoPanel = getInfoPanel(child, activeOnly)) != null)) {
                    break;
                }
            }
            
            if (infoPanel == null) {
                for (UIElementBase child : parent.getChildren()) {
                    if ((child != exclude) && ((infoPanel = searchChildren(child, null, activeOnly)) != null)) {
                        break;
                    }
                }
            }
        }
        
        return infoPanel;
    }
    
    /**
     * Returns the info panel associated with the UI element, if there is one.
     * 
     * @param element The element to examine.
     * @param activeOnly If true, only active info panels are considered.
     * @return The associated active info panel, or null if there is none.
     */
    private static IInfoPanel getInfoPanel(UIElementBase element, boolean activeOnly) {
        if ((element instanceof UIElementPlugin) && (!activeOnly || element.isActivated())
                && ((UIElementPlugin) element).getDefinition().getId().equals("infoPanelPlugin")) {
            PluginContainer container = ((UIElementPlugin) element).getContainer();
            container.load();
            BaseComponent top = container.findByName("infoPanelRoot");
            return (IInfoPanel) FrameworkController.getController(top);
        }
        
        return null;
    }
    
    /**
     * Associate a generic event with an action on this component's container.
     * 
     * @param component Component whose container is the target of an action.
     * @param eventName The name of the generic event that will invoke the action.
     * @param action The action to be performed.
     */
    public static void associateEvent(BaseComponent component, String eventName, Action action) {
        getActionListeners(component, true).add(new ActionListener(eventName, action));
    }
    
    /**
     * Returns a list of events associated with a component. Should only be used internally.
     * 
     * @param component Component of interest.
     * @return The list of associated events (may be null).
     */
    public static List<ActionListener> getActionListeners(BaseComponent component) {
        return getActionListeners(component, false);
    }
    
    /**
     * Returns a list of events associated with a component.
     * 
     * @param component Component of interest.
     * @param forceCreate If true and no current list exists, one will be created.
     * @return The list of associated events (may be null).
     */
    private static List<ActionListener> getActionListeners(BaseComponent component, boolean forceCreate) {
        @SuppressWarnings("unchecked")
        List<ActionListener> ActionListeners = (List<ActionListener>) component.getAttribute(EVENT_LISTENER_ATTR);
        
        if (ActionListeners == null && forceCreate) {
            ActionListeners = new ArrayList<>();
            component.setAttribute(EVENT_LISTENER_ATTR, ActionListeners);
        }
        
        return ActionListeners;
    }
    
    /**
     * Enforce static class.
     */
    private InfoPanelService() {
    }
    
}
