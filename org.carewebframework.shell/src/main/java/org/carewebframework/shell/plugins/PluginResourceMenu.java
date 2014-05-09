/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.zkoss.zul.Menu;

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
     * @return The action to be invoked. This may be a url or a zscript action.
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the action that will be invoked when the menu item is clicked.
     * 
     * @param action The action to be invoked. This may be a url or a zscript action.
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
     * Registers the menu resource with the container.
     */
    @Override
    public void process(PluginContainer container) {
        Menu menu = container.getShell().addMenu(path, action);
        container.registerComponent(menu);
        container.registerId(id, menu);
    }
}
