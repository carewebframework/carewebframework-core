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

import org.carewebframework.ui.action.ActionRegistry;

/**
 * Resource for declaring actions supported by a plugin.
 */
public class PluginResourceAction implements IPluginResource {
    
    // The unique id of the action.
    private String id;
    
    // The label of the action.
    private String label;
    
    // The script of the action
    private String script;
    
    /**
     * Returns the unique id associated with the action.
     * 
     * @return The action's unique id.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the unique id associated with the action.
     * 
     * @param id The action's unique id.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Returns the label of the action.
     * 
     * @return The action's label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Sets the label of the action.
     * 
     * @param label The new label.
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Returns the script of the action.
     * 
     * @return The action's script.
     */
    public String getScript() {
        return script;
    }
    
    /**
     * Sets the script of the action.
     * 
     * @param script The new script.
     */
    public void setScript(String script) {
        this.script = script;
    }
    
    /**
     * Registers the action resource with the action registry.
     */
    @Override
    public void process(PluginContainer container) {
        ActionRegistry.register(false, id, label, script);
    }
}
