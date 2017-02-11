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
import org.carewebframework.shell.elements.UIElementBase;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.web.component.BaseUIComponent;

/**
 * Resource for declaring commands supported by a plugin.
 */
public class PluginResourceCommand implements IPluginResource {
    
    // The name of the command.
    private String name;
    
    /**
     * Returns the name of the command.
     * 
     * @return The command's name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the command.
     * 
     * @param name The command's name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Registers/unregisters a command resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, UIElementBase owner, boolean register) {
        if (register) {
            CommandUtil.associateCommand(getName(), (BaseUIComponent) owner.getOuterComponent());
        }
    }
    
}
