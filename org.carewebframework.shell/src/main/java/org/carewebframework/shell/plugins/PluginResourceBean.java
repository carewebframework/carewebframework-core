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
     * Registers the css resource with the container.
     */
    @Override
    public void process(PluginContainer container) {
        container.registerBean(bean, required);
    }
    
}
