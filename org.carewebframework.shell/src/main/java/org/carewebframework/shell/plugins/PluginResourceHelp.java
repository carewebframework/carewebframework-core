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

import org.carewebframework.help.HelpModule;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.elements.ElementBase;

/**
 * Resource for declaring items to appear under the help menu.
 * <p>
 * There are essentially two ways to declare a help resource, one for internal help resources and
 * one for external help resources:
 * <ul>
 * <li>If the help resource uses the framework's embedded help system, you only need to set the
 * module name (and optionally the topic). Default values for all other properties can be derived
 * from the associated help definition, though you can set them explicitly if you need to override
 * the defaults.</li>
 * <li>If the help resource refers to externally derived help content (e.g., another web page), you
 * will instead need to set the path and associated action. In this case, the module name should
 * remain empty.</li>
 * </ul>
 */
public class PluginResourceHelp implements IPluginResource {
    
    // Determines where the menu item will appear under the help submenu.
    private String path;
    
    // The action to be invoked when the help menu item is clicked.
    private String action;
    
    // An optional topic within the help module.  If not specified, the default topic is displayed.
    private String topic;
    
    // The unique id of the help module.
    private String id;
    
    /**
     * Returns the path that determines where the associated menu item will appear under the help
     * submenu. If this value has not been explicitly set, and a help module has been specified, its
     * value will be determined from the help module.
     * 
     * @return The path that determines where the associated menu item will appear under the help
     *         submenu. A menu hierarchy can be specified by separating levels with backslash
     *         characters.
     */
    public String getPath() {
        if (path == null) {
            HelpModule module = HelpModule.getModule(id);
            path = module == null ? "" : module.getTitle();
        }
        
        return path;
    }
    
    /**
     * Sets the path that determines where the associated menu item will appear under the help
     * submenu. This is typically only specified for external help resources.
     * 
     * @param path The path that determines where the associated menu item will appear under the
     *            help submenu. A menu hierarchy can be specified by separating levels with
     *            backslash characters.
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Returns the action that will be invoked when the associated menu item is clicked. For
     * externally derived help content, this value must be explicitly set. Otherwise, it will be
     * determined automatically from the specified help module.
     * 
     * @return The action to be invoked to display the help content. This may be a url or a groovy
     *         script action.
     */
    public String getAction() {
        if (action == null) {
            HelpModule module = HelpModule.getModule(id);
            action = module == null ? ""
                    : "groovy:" + CareWebUtil.class.getName() + ".showHelpTopic(\"" + id + "\",\""
                            + (topic == null ? "" : topic) + "\",\"" + module.getTitle() + "\");";
        }
        
        return action;
    }
    
    /**
     * Sets the action that will be invoked when the associated menu item is clicked. For externally
     * derived help content, this value must be explicitly set. For internally derived content, do
     * not set this value. It will be derived automatically from the specified help module.
     * 
     * @param action The action to be invoked to display the help content. This may be a url or a
     *            groovy script action.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the topic id associated with this help resource. Specifying a topic id is optional
     * and only applicable to internally derived help content. If a topic id is specified,
     * information related to that topic will be displayed. Otherwise, the default topic will be
     * displayed.
     * 
     * @return The id of the topic to be displayed, or empty to display the default topic.
     */
    public String getTopic() {
        return topic;
    }
    
    /**
     * Sets the topic id associated with this help resource. Specifying a topic id is optional and
     * only applicable to internally derived help content. If a topic id is specified, information
     * related to that topic will be displayed. Otherwise, the default topic will be displayed.
     * 
     * @param topic The id of the topic to be displayed, or empty to display the default topic.
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    /**
     * Returns the id of the associated help module. This is only applicable for internally derived
     * help content.
     * 
     * @return The id of the associated help module.
     */
    public String getModule() {
        return id;
    }
    
    /**
     * Sets the id of the associated help module. This is only applicable for internally derived
     * help content.
     * 
     * @param module The id of the associated help module.
     */
    public void setModule(String module) {
        this.id = module;
    }
    
    /**
     * Registers/unregisters a help resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, ElementBase owner, boolean register) {
        if (register) {
            shell.registerHelpResource(this);
        }
    }
    
}
