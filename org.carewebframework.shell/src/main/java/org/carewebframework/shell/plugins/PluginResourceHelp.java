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

import org.carewebframework.help.HelpModule;
import org.carewebframework.help.viewer.HelpUtil;
import org.carewebframework.shell.CareWebShell;

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
     * @return The action to be invoked to display the help content. This may be a url or a zscript
     *         action.
     */
    public String getAction() {
        if (action == null) {
            HelpModule module = HelpModule.getModule(id);
            action = module == null ? ""
                    : "zscript:" + HelpUtil.class.getName() + ".show(\"" + id + "\",\"" + (topic == null ? "" : topic)
                            + "\",\"" + module.getTitle() + "\");";
        }
        
        return action;
    }
    
    /**
     * Sets the action that will be invoked when the associated menu item is clicked. For externally
     * derived help content, this value must be explicitly set. For internally derived content, do
     * not set this value. It will be derived automatically from the specified help module.
     * 
     * @param action The action to be invoked to display the help content. This may be a url or a
     *            zscript action.
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
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, boolean register) {
        if (register) {
            shell.registerHelpResource(this);
        }
    }
    
}
