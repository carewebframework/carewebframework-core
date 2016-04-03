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

import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.ui.command.CommandUtil;

import org.zkoss.zul.impl.XulElement;

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
            CommandUtil.associateCommand(getName(), (XulElement) owner.getOuterComponent());
        }
    }
    
}
