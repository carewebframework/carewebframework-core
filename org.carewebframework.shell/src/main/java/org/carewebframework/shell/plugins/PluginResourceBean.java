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
import org.carewebframework.shell.layout.UIElementPlugin;

/**
 * Resource for declaring helper beans associated with the plugin.
 */
public class PluginResourceBean implements IPluginResource {
    
    
    private String bean;
    
    private boolean required = true;
    
    /**
     * Sets the referenced bean by its id.
     * 
     * @param bean The bean id.
     */
    public void setBean(String bean) {
        this.bean = bean;
    }
    
    /**
     * Gets the id of the referenced bean.
     * 
     * @return The bean id.
     */
    public String getBean() {
        return bean;
    }
    
    /**
     * Sets whether or not the reference bean is required.
     * 
     * @param required If true and the bean is not found, an exception is raised.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    /**
     * Returns whether or not the reference bean is required.
     * 
     * @return If true and the bean is not found, an exception is raised.
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * Registers/unregisters a bean resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, UIElementBase owner, boolean register) {
        if (register) {
            UIElementPlugin plugin = (UIElementPlugin) owner;
            plugin.getContainer().registerBean(getBean(), isRequired());
        }
    }
    
}
