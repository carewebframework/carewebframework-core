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
package org.carewebframework.shell.plugins;

import org.carewebframework.shell.elements.UIElementPlugin;
import org.carewebframework.web.event.Event;

/**
 * Event for all plugin-related actions.
 */
public class PluginEvent extends Event {
    
    public static final String TYPE = "action";
    
    /**
     * Actions that may be performed on the container.
     */
    public enum PluginAction {
        SUBSCRIBE, LOAD, ACTIVATE, INACTIVATE, UNLOAD, UNSUBSCRIBE
    }
    
    private final PluginAction action;
    
    private final UIElementPlugin plugin;
    
    /**
     * Creates an event to encapsulate an action on the specified plugin.
     * 
     * @param plugin Plugin receiving the action.
     * @param action The action performed.
     */
    public PluginEvent(UIElementPlugin plugin, PluginAction action) {
        this(plugin, action, null);
    }
    
    /**
     * Creates an event to encapsulate an action on the specified plugin.
     * 
     * @param plugin Plugin receiving the action.
     * @param action The action performed.
     * @param data Arbitrary data to attach.
     */
    public PluginEvent(UIElementPlugin plugin, PluginAction action, Object data) {
        super(TYPE, plugin.getOuterComponent(), data);
        this.plugin = plugin;
        this.action = action;
    }
    
    /**
     * Returns the action performed.
     * 
     * @return The plugin action.
     */
    public PluginAction getAction() {
        return action;
    }
    
    /**
     * Returns the plugin upon which the action is performed.
     * 
     * @return The plugin.
     */
    public UIElementPlugin getPlugin() {
        return plugin;
    }
}
