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

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementPlugin;
import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.web.component.Button;

/**
 * Resource for declaring buttons to appear on common toolbar.
 */
public class PluginResourceButton implements IPluginResource {
    
    // The caption text for the button.
    private String caption;
    
    // The action to be invoked when the button is clicked.
    private String action;
    
    // Text to appear when the mouse cursor hovers over the button.
    private String hint;
    
    // The url of the icon to appear on the button.
    private String icon;
    
    // Optional component name.
    private String name;
    
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
    public String getHint() {
        return hint;
    }
    
    /**
     * Sets the text to appear when the mouse cursor hovers over the button.
     * 
     * @param hint The tool tip text.
     */
    public void setHint(String hint) {
        this.hint = hint;
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
     * Returns the name to be assigned to the newly created component.
     * 
     * @return Component name (may be null).
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name to be assigned to the newly created component.
     * 
     * @param name Component name (may be null).
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Registers/unregisters a button resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, UIElementBase owner, boolean register) {
        if (register) {
            UIElementPlugin plugin = (UIElementPlugin) owner;
            PluginContainer container = plugin.getContainer();
            Button button = new Button(getCaption());
            button.setName(getName());
            button.setHint(getHint());
            //button.setImage(getIcon());
            ActionListener.addAction(button, getAction());
            container.addToolbarComponent(button);
            container.registerId(getName(), button);
        }
    }
    
}
