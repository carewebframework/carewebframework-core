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

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;

/**
 * Resource for declaring buttons to appear on common toolbar.
 */
public class PluginResourceButton implements IPluginResource {
    
    
    // The caption text for the button.
    private String caption;
    
    // The action to be invoked when the button is clicked.
    private String action;
    
    // Text to appear when the mouse cursor hovers over the button.
    private String tooltip;
    
    // The url of the icon to appear on the button.
    private String icon;
    
    // Optional component id.
    private String id;
    
    /**
     * Returns the value of the button's caption text.
     * 
     * @return The button's caption text.
     */
    public String getCaption() {
        return caption != null && caption.toLowerCase().startsWith("label:") ? StrUtil.getLabel(caption.substring(6))
                : caption;
    }
    
    /**
     * Sets the value of the button's caption text.
     * 
     * @param caption The button's caption text.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * Returns the action that will be invoked when the button is clicked.
     * 
     * @return The action to be invoked. This may be a url or a zscript action.
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the action that will be invoked when the button is clicked.
     * 
     * @param action The action to be invoked. This may be a url or a zscript action.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the text to appear when the mouse cursor hovers over the button.
     * 
     * @return The tool tip text.
     */
    public String getTooltip() {
        return tooltip;
    }
    
    /**
     * Sets the text to appear when the mouse cursor hovers over the button.
     * 
     * @param tooltip The tool tip text.
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }
    
    /**
     * Returns the url of an image to display on the button.
     * 
     * @return Url of the image.
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * Sets the url of an image to display on the button.
     * 
     * @param icon Url of the image.
     */
    public void setIcon(String icon) {
        this.icon = icon;
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
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, boolean register) {
    }
    
}
