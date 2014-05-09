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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.help.HelpDefinition;
import org.carewebframework.shell.help.HelpRegistry;
import org.carewebframework.shell.help.HelpUtil;

import org.zkoss.util.resource.Labels;

/**
 * Base abstract class for all plugin-associated resources.
 */
public abstract class PluginResource {
    
    /**
     * Delegates the processing of a plugin resource to its container.
     * 
     * @param container A plugin container.
     */
    public abstract void process(PluginContainer container);
    
    /**
     * Resource for declaring items to appear under the help menu.
     * <p>
     * There are essentially two ways to declare a help resource, one for internal help resources
     * and one for external help resources:
     * <ul>
     * <li>If the help resource uses the framework's embedded help system, you only need to set the
     * module name (and optionally the topic). Default values for all other properties can be
     * derived from the associated help definition, though you can set them explicitly if you need
     * to override the defaults.</li>
     * <li>If the help resource refers to externally derived help content (e.g., another web page),
     * you will instead need to set the path and associated action. In this case, the module name
     * should remain empty.</li>
     * </ul>
     */
    public static class HelpResource extends PluginResource {
        
        // Determines where the menu item will appear under the help submenu.
        private String path;
        
        // The action to be invoked when the help menu item is clicked.
        private String action;
        
        // An optional topic within the help module.  If not specified, the default topic is displayed.
        private String topic;
        
        // The name of the help module if using the embedded help system.
        private String module;
        
        /**
         * Returns the path that determines where the associated menu item will appear under the
         * help submenu. If this value has not been explicitly set, and a help module has been
         * specified, its value will be determined from the help module.
         * 
         * @return The path that determines where the associated menu item will appear under the
         *         help submenu. A menu hierarchy can be specified by separating levels with
         *         backslash characters.
         */
        public String getPath() {
            if (path == null) {
                HelpDefinition def = getHelpDefinition();
                path = def == null ? "" : def.getTitle();
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
         * @return The action to be invoked to display the help content. This may be a url or a
         *         zscript action.
         */
        public String getAction() {
            if (action == null) {
                HelpDefinition def = getHelpDefinition();
                action = def == null ? "" : "zscript:" + HelpUtil.class.getName() + ".show(\"" + module + "\",\""
                        + (topic == null ? "" : topic) + "\",\"" + def.getTitle() + "\");";
            }
            
            return action;
        }
        
        /**
         * Sets the action that will be invoked when the associated menu item is clicked. For
         * externally derived help content, this value must be explicitly set. For internally
         * derived content, do not set this value. It will be derived automatically from the
         * specified help module.
         * 
         * @param action The action to be invoked to display the help content. This may be a url or
         *            a zscript action.
         */
        public void setAction(String action) {
            this.action = action;
        }
        
        /**
         * Returns the topic id associated with this help resource. Specifying a topic id is
         * optional and only applicable to internally derived help content. If a topic id is
         * specified, information related to that topic will be displayed. Otherwise, the default
         * topic will be displayed.
         * 
         * @return The id of the topic to be displayed, or empty to display the default topic.
         */
        public String getTopic() {
            return topic;
        }
        
        /**
         * Sets the topic id associated with this help resource. Specifying a topic id is optional
         * and only applicable to internally derived help content. If a topic id is specified,
         * information related to that topic will be displayed. Otherwise, the default topic will be
         * displayed.
         * 
         * @param topic The id of the topic to be displayed, or empty to display the default topic.
         */
        public void setTopic(String topic) {
            this.topic = topic;
        }
        
        /**
         * Returns the id of the associated help module. This is only applicable for internally
         * derived help content.
         * 
         * @return The id of the associated help module.
         */
        public String getModule() {
            return module;
        }
        
        /**
         * Sets the id of the associated help module. This is only applicable for internally derived
         * help content.
         * 
         * @param module The id of the associated help module.
         */
        public void setModule(String module) {
            this.module = module;
        }
        
        /**
         * Returns the help definition of the associated help module, if one has been specified.
         * 
         * @return The help definition of the associated help module. It will be null if a module
         *         has not been specified, or the module has not been registered.
         */
        public HelpDefinition getHelpDefinition() {
            return StringUtils.isEmpty(module) ? null : HelpRegistry.getInstance().get(module);
        }
        
        /**
         * Registers the help resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring buttons to appear on common toolbar.
     */
    public static class ButtonResource extends PluginResource {
        
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
            return caption != null && caption.toLowerCase().startsWith("label:") ? Labels.getLabel(caption.substring(6))
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
         * Registers the button resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring property groups associated with the plugin. This information can be
     * used by components that manage user preferences, for example.
     */
    public static class PropertyResource extends PluginResource {
        
        // The name of the property group.
        private String group;
        
        /**
         * Returns the name of the associated property group.
         * 
         * @return The property group name.
         */
        public String getGroup() {
            return group;
        }
        
        /**
         * Sets the name of the associated property group.
         * 
         * @param group The property group name.
         */
        public void setGroup(String group) {
            this.group = group;
        }
        
        /**
         * Registers the property resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring items to appear on the common menu.
     */
    public static class MenuResource extends PluginResource {
        
        // Determines where the menu item will appear on the common menu.
        private String path;
        
        // The action to be invoked when the menu item is clicked.
        private String action;
        
        // Optional component id.
        private String id;
        
        /**
         * Returns the path that determines where the associated menu item will appear under the
         * common menu.
         * 
         * @return The path that determines where the associated menu item will appear under the
         *         common menu. A menu hierarchy can be specified by separating levels with
         *         backslash characters.
         */
        public String getPath() {
            return path;
        }
        
        /**
         * Sets the path that determines where the associated menu item will appear under the common
         * menu.
         * 
         * @param path The path that determines where the associated menu item will appear under the
         *            common menu. A menu hierarchy can be specified by separating levels with
         *            backslash characters.
         */
        public void setPath(String path) {
            this.path = path;
        }
        
        /**
         * Returns the action that will be invoked when the menu item is clicked.
         * 
         * @return The action to be invoked. This may be a url or a zscript action.
         */
        public String getAction() {
            return action;
        }
        
        /**
         * Sets the action that will be invoked when the menu item is clicked.
         * 
         * @param action The action to be invoked. This may be a url or a zscript action.
         */
        public void setAction(String action) {
            this.action = action;
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
         * Registers the menu resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring style sheets associated with the plugin.
     */
    public static class CSSResource extends PluginResource {
        
        // The url of the style sheet.
        private String url;
        
        /**
         * Returns the url of the associated style sheet.
         * 
         * @return A url.
         */
        public String getUrl() {
            return url;
        }
        
        /**
         * Sets the url of the associated style sheet.
         * 
         * @param url The url.
         */
        public void setUrl(String url) {
            this.url = url;
        }
        
        /**
         * Registers the css resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring helper beans associated with the plugin.
     */
    public static class BeanResource extends PluginResource {
        
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
            container.processResource(this);
        }
        
    }
    
    /**
     * Resource for declaring commands supported by a plugin.
     */
    public static class CommandResource extends PluginResource {
        
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
         * Registers the command resource with the container.
         */
        @Override
        public void process(PluginContainer container) {
            container.processResource(this);
        }
    }
    
    /**
     * Resource for declaring actions supported by a plugin.
     */
    public static class ActionResource extends PluginResource {
        
        // The label of the action.
        private String label;
        
        // The script of the action
        private String script;
        
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
            container.processResource(this);
        }
    }
    
}
