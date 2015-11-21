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

/**
 * Resource for declaring property groups associated with the plugin. This information can be used
 * by components that manage user preferences, for example.
 */
public class PluginResourcePropertyGroup implements IPluginResource {
    
    // The name of the property group.
    private String group;
    
    /**
     * Returns the name of the associated property group.
     * 
     * @return The property group name.
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Sets the name of the associated property group.
     * 
     * @param group The property group name.
     */
    public void setGroup(String group) {
        this.group = group;
    }
    
}
