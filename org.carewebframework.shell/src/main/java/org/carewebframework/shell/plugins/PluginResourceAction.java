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
import org.carewebframework.shell.layout.UIElementBase;
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
     * Registers/unregisters an action resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, UIElementBase owner, boolean register) {
        if (register) {
            ActionRegistry.register(false, getId(), getLabel(), getScript());
        }
    }
    
}
