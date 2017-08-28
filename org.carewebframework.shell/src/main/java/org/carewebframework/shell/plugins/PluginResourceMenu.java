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

import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementPlugin;
import org.fujion.component.BaseMenuComponent;

/**
 * Resource for declaring items to appear on the common menu.
 */
public class PluginResourceMenu implements IPluginResource {
    
    // Determines where the menu item will appear on the common menu.
    private String path;
    
    // The action to be invoked when the menu item is clicked.
    private String action;
    
    // Optional component id.
    private String id;
    
    /**
     * Returns the path that determines where the associated menu item will appear under the common
     * menu.
     * 
     * @return The path that determines where the associated menu item will appear under the common
     *         menu. A menu hierarchy can be specified by separating levels with backslash
     *         characters.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Sets the path that determines where the associated menu item will appear under the common
     * menu.
     * 
     * @param path The path that determines where the associated menu item will appear under the
     *            common menu. A menu hierarchy can be specified by separating levels with backslash
     *            characters.
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Returns the action that will be invoked when the menu item is clicked.
     * 
     * @return The action to be invoked. This may be a url or a groovy script action.
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the action that will be invoked when the menu item is clicked.
     * 
     * @param action The action to be invoked. This may be a url or a groovy script action.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the id to be assigned to the newly created component.
     * 
     * @return Component id (may be null).
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the id to be assigned to the newly created component.
     * 
     * @param id Component id (may be null).
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Registers/unregisters a plugin resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, ElementBase owner, boolean register) {
        if (register) {
            ElementPlugin plugin = (ElementPlugin) owner;
            BaseMenuComponent menu = plugin.getShell().addMenu(getPath(), getAction());
            plugin.registerComponent(menu);
            plugin.registerId(getId(), menu);
        }
    }
    
}
