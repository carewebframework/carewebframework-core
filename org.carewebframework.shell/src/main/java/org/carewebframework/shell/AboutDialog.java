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
package org.carewebframework.shell;

import java.util.LinkedHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ManifestViewer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.metainfo.PageDefinition;

/**
 * Displays an "about" dialog for a given UI element.
 */
public class AboutDialog extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Internal class for passing about attributes to about dialog.
     */
    @SuppressWarnings("serial")
    private static class AboutParams extends LinkedHashMap<Object, Object> {
        
        private String icon;
        
        private String title;
        
        private String source;
        
        private String custom;
        
        private String description;
        
        public AboutParams(PluginDefinition def) {
            super();
            title = def.getName();
            source = def.getSource();
            icon = def.getIcon();
            description = def.getDescription();
            set("name", def.getName());
            set("version", def.getVersion());
            set("creator", def.getCreator());
            set("copyright", def.getCopyright());
            set("release", def.getReleased());
        }
        
        public AboutParams(Manifest manifest) {
            super();
            
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                title = attributes.getValue("Implementation-Title");
                source = attributes.getValue("Implementation-Vendor");
                custom = getLabel("installation.details");
                description = attributes.getValue("Description");
                set("name", title);
                set("version", attributes.getValue("Implementation-Version"));
            }
        }
        
        public AboutParams(Class<?> clazz) {
            super();
            Package pkg = clazz.getPackage();
            String name = clazz.getSimpleName();
            title = pkg.getImplementationTitle();
            title = StringUtils.isEmpty(title) ? name : title;
            source = pkg.getImplementationVendor();
            set("name", name);
            set("pkg", pkg.getName());
            set("version", pkg.getImplementationVersion());
        }
        
        /**
         * Add first non-null/non-empty value to map.
         * 
         * @param key The key to be added (is translated to a label value).
         * @param values The list of possible values.
         */
        private void set(String key, String... values) {
            String value = get(values);
            
            if (value != null) {
                put(getLabel(key), value);
            }
        }
        
        /**
         * Return first non-null/non-empty value.
         * 
         * @param values The list of possible values.
         * @return The first non-null/non-empty value, or null if there are none.
         */
        private String get(String... values) {
            if (values != null) {
                for (String value : values) {
                    if (!StringUtils.isEmpty(value)) {
                        return value;
                    }
                }
            }
            return null;
        }
        
        /**
         * Returns the label value for the specified key.
         * 
         * @param key Key for label.
         * @return The label value, or the original key value if no label value found.
         */
        private String getLabel(String key) {
            String label = StrUtil.getLabel("cwf.shell.about." + key.toString());
            return label == null ? key : label;
        }
    }
    
    /**
     * Display an about dialog for the specified UI element.
     * 
     * @param component UI element whose attributes are to be displayed.
     */
    public static void execute(UIElementBase component) {
        if (component.getDefinition() != null) {
            execute(component.getDefinition());
        } else {
            execute(component.getClass());
        }
    }
    
    /**
     * Display an about dialog for the specified class.
     * 
     * @param clazz Class whose attributes are to be displayed.
     */
    public static void execute(Class<?> clazz) {
        showDialog(new AboutParams(clazz));
    }
    
    /**
     * Display an about dialog for the specified plugin definition.
     * 
     * @param def Plugin definition whose attributes are to be displayed.
     */
    public static void execute(PluginDefinition def) {
        showDialog(new AboutParams(def));
    }
    
    /**
     * Display an about dialog for the specified manifest.
     * 
     * @param manifest Manifest whose attributes are to be displayed.
     */
    public static void execute(Manifest manifest) {
        showDialog(new AboutParams(manifest));
    }
    
    /**
     * Common entry point for displaying an about dialog.
     * 
     * @param params Parameter map for about dialog.
     */
    private static void showDialog(AboutParams params) {
        try {
            PageDefinition pageDefinition = ZKUtil.loadCachedPageDefinition(Constants.RESOURCE_PREFIX + "aboutDialog.zul");
            PopupDialog.popup(pageDefinition, params, true, false, true);
        } catch (Exception e) {
            PromptDialog.showError(ZKUtil.formatExceptionForDisplay(e));
        }
    }
    
    private AboutParams aboutParams;
    
    private String source;
    
    private String icon;
    
    private Component cellDescription;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        if (!StringUtils.isEmpty(aboutParams.description)) {
            cellDescription.appendChild(ZKUtil.getTextComponent(aboutParams.description));
            cellDescription.getParent().setVisible(true);
        }
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
        aboutParams = (AboutParams) Executions.getCurrent().getArg();
    }
    
    /**
     * Display the manifest viewer when detail button is clicked.
     */
    public void onClick$btnCustom() {
        ManifestViewer.execute();
    }
    
    /**
     * Returns the url of the icon to display in the header.
     * 
     * @return Icon url.
     */
    public String getIcon() {
        return aboutParams.icon == null ? icon : aboutParams.icon;
    }
    
    /**
     * Sets the default url of the icon to display in the header.
     * 
     * @param icon Icon url.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * Returns the item source to display in the header.
     * 
     * @return Item source.
     */
    public String getSource() {
        return aboutParams.source == null ? source : aboutParams.source;
    }
    
    /**
     * Sets the default item source to display in the header.
     * 
     * @param source Item source.
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * Returns the label for the custom button.
     * 
     * @return The label for the custom button. If null, the custom button remains hidden.
     */
    public String getCustom() {
        return aboutParams.custom;
    }
    
    /**
     * Returns the subtitle text.
     * 
     * @return Subtitle text.
     */
    public String getTitle() {
        return aboutParams.title;
    }
}
