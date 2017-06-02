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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.ManifestIterator;
import org.carewebframework.api.property.IPropertyProvider;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.shell.ancillary.UIException;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.shell.property.PropertyInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

/**
 * Each instance of this class defines a complete definition of a CareWeb plugin. A definition
 * declares various attributes of a plugin, like its display name, url, toolbar buttons, help
 * module, etc.
 */
public class PluginDefinition {
    
    private static final Log log = LogFactory.getLog(PluginDefinition.class);
    
    /**
     * Represents a security authority.
     */
    public static class Authority {
        
        private String name;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private String name;
    
    private String source;
    
    private String url;
    
    private String id;
    
    private String description;
    
    private String category;
    
    private String creator;
    
    private String copyright;
    
    private String version;
    
    private String released;
    
    private String icon;
    
    private boolean requiresAll;
    
    private boolean lazyLoad = true;
    
    private boolean disabled;
    
    private Class<? extends ElementBase> clazz;
    
    private final List<IPluginResource> resources = new ArrayList<>();
    
    private final List<Authority> authorities = new ArrayList<>();
    
    private final List<PropertyInfo> properties = new ArrayList<>();
    
    private Manifest manifest;
    
    /**
     * Returns the plugin definition associated with the specified xml tag (same as plugin id).
     * 
     * @param tag XML tag (plugin id).
     * @return The associated plugin definition, or null if not found.
     */
    public static PluginDefinition getDefinition(String tag) {
        return PluginRegistry.getInstance().get(tag);
    }
    
    /**
     * Returns the plugin definition associated with the specified class.
     * 
     * @param clazz The class whose plugin definition is sought.
     * @return The associated plugin definition, or null if not found.
     */
    public static PluginDefinition getDefinition(Class<? extends ElementBase> clazz) {
        return PluginRegistry.getInstance().get(clazz);
    }
    
    /**
     * The basic constructor.
     */
    public PluginDefinition() {
        
    }
    
    /**
     * Constructs a plugin definition for the specified plugin name and class.
     * 
     * @param name Display name of the plugin.
     * @param clazz Associated UI element class.
     * @throws ClassNotFoundException If class not found.
     */
    public PluginDefinition(String name, Class<? extends ElementBase> clazz) throws ClassNotFoundException {
        super();
        this.name = name;
        setClazz(clazz);
    }
    
    /**
     * Copy constructor.
     * 
     * @param def Plugin definition to copy.
     */
    public PluginDefinition(PluginDefinition def) {
        BeanUtils.copyProperties(def, this);
    }
    
    /**
     * Returns the unique id of the plugin definition.
     * 
     * @return The unique id.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the unique id of the plugin definition
     * 
     * @param id The unique plugin id.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Returns the source of the plugin.
     * 
     * @return The plugin source.
     */
    public String getSource() {
        return getValueWithDefault(source, "Implementation-Vendor");
    }
    
    /**
     * Sets the source of the plugin.
     * 
     * @param source Plugin source.
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * Returns the display name of the plugin definition.
     * 
     * @return The display name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the display name of the plugin definition.
     * 
     * @param name Plugin display name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the URL of the principal cwf page for the plugin.
     * 
     * @return The URL of the principal cwf page.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Sets the URL of the principal cwf page for the plugin.
     * 
     * @param url The URL of the principal cwf page.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Sets the requiresAll flag. When true, this flag indicates that all associated authorities
     * must be present in order to access the plugin. When false, any single associated authority is
     * sufficient for access. If no authorities are associated with the plugin, this setting is
     * ignored.
     * 
     * @param requiresAll The requiresAll flag.
     */
    public void setRequiresAll(boolean requiresAll) {
        this.requiresAll = requiresAll;
    }
    
    /**
     * Returns the requiresAll flag. When true, this flag indicates that all associated authorities
     * must be present in order to access the plugin. When false, any single associated authority is
     * sufficient for access. If no authorities are associated with the plugin, this setting is
     * ignored.
     * 
     * @return The requiresAll flag.
     */
    public boolean isRequiresAll() {
        return requiresAll;
    }
    
    /**
     * Returns the lazyLoad flag. When true, a plugin created from this definition is not fully
     * initialized until it is first activity. When false, the plugin is initialized immediately
     * after deserialization.
     * 
     * @return The lazyLoad flag.
     */
    public boolean isLazyLoad() {
        return lazyLoad;
    }
    
    /**
     * Sets the lazyLoad flag. When true, a plugin created from this definition is not fully
     * initialized until it is first activity. When false, the plugin is initialized immediately
     * after deserialization.
     * 
     * @param lazyLoad The lazyLoad flag.
     */
    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }
    
    /**
     * Returns the list of associated plugin resources. Never null.
     * 
     * @return List of associated resources.
     */
    public List<IPluginResource> getResources() {
        return resources;
    }
    
    /**
     * Returns the list of plugin resources belonging to the specified resource class. Never null.
     * 
     * @param <E> A subclass of PluginResource.
     * @param clazz The resource class being sought.
     * @return List of associated resources.
     */
    @SuppressWarnings("unchecked")
    public <E extends IPluginResource> List<E> getResources(Class<E> clazz) {
        List<E> list = new ArrayList<>();
        
        for (IPluginResource resource : resources) {
            if (clazz.isInstance(resource)) {
                list.add((E) resource);
            }
        }
        
        return list;
    }
    
    /**
     * Adds resources from the list to the list of resources associated with this plugin.
     * 
     * @param resources Resources to add.
     */
    public void setResources(List<IPluginResource> resources) {
        this.resources.addAll(resources);
    }
    
    /**
     * Returns the list of authorities required for access to this plugin. Never null.
     * 
     * @return List of authorities.
     */
    public List<Authority> getAuthorities() {
        return authorities;
    }
    
    /**
     * Adds authorities from the list to the list of authorities associated with this plugin.
     * 
     * @param authorities Authorities required by this plugin.
     */
    public void setAuthorities(List<Authority> authorities) {
        this.authorities.addAll(authorities);
    }
    
    /**
     * Returns the list of properties associated with this plugin. Never null.
     * 
     * @return List of properties.
     */
    public List<PropertyInfo> getProperties() {
        return properties;
    }
    
    /**
     * Adds properties from the list to the list of properties associated with this plugin.
     * 
     * @param properties List of associated properties.
     */
    public void setProperties(List<PropertyInfo> properties) {
        this.properties.addAll(properties);
    }
    
    /**
     * Returns the UI element class associated with this definition.
     * 
     * @return Associated UI element class.
     */
    public Class<? extends ElementBase> getClazz() {
        if (clazz == null) {
            setClazz(ElementPlugin.class);
        }
        
        return clazz;
    }
    
    /**
     * Sets the UI element class associated with this definition.
     * 
     * @param clazz The associated class.
     */
    public void setClazz(Class<? extends ElementBase> clazz) {
        this.clazz = clazz;
        
        try {
            // Force execution of static initializers
            Class.forName(clazz.getName());
        } catch (ClassNotFoundException e) {
            MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Returns the description of this plugin.
     * 
     * @return Plugin description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of this plugin.
     * 
     * @param description The plugin description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Sets the category under which this plugin is to be classified. This can be specified as a
     * tree node path using "\" to separate the path components.
     * 
     * @param category The plugin category.
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Returns the category under which this plugin is to be classified. This can be specified as a
     * tree node path using "\" to separate the path components.
     * 
     * @return Associated category.
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Returns the name of the creator of this plugin.
     * 
     * @return Plugin creator.
     */
    public String getCreator() {
        return creator;
    }
    
    /**
     * Sets the name of the creator of this plugin.
     * 
     * @param creator The plugin creator.
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    /**
     * Returns any copyright information for this plugin.
     * 
     * @return Copyright info.
     */
    public String getCopyright() {
        return copyright;
    }
    
    /**
     * Sets any copyright information for this plugin.
     * 
     * @param copyright Plugin copyright information.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    /**
     * Returns version information about the plugin.
     * 
     * @return Version info.
     */
    public String getVersion() {
        return getValueWithDefault(version, "Implementation-Version");
    }
    
    /**
     * Sets version information about the plugin.
     * 
     * @param version The plugin version.
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Sets release information (typically a date) about the plugin.
     * 
     * @param released Release information.
     */
    public void setReleased(String released) {
        this.released = released;
    }
    
    /**
     * Returns release information (typically a date) about the plugin.
     * 
     * @return Release info.
     */
    public String getReleased() {
        return released;
    }
    
    /**
     * Sets the url of the icon associated with the plugin.
     * 
     * @param icon Url of an icon.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * Returns the url of the icon associated with the plugin.
     * 
     * @return Url of associated icon.
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * Returns true if this definition represents an internal UI element (i.e., one that has been
     * pre-created by some other means).
     * 
     * @return True if internal element.
     */
    public boolean isInternal() {
        return id != null && id.startsWith("_");
    }
    
    /**
     * Returns true if the plugin has been disabled.
     * 
     * @return True if disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Sets the disabled state of the plugin.
     * 
     * @param disabled The desired state.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    /**
     * Returns true if access to the plugin is restricted.
     * 
     * @return True if access to the plugin is restricted.
     */
    public boolean isForbidden() {
        if (authorities.size() == 0) {
            return false; // If no restrictions, return false
        }
        
        boolean result = true;
        
        for (Authority priv : authorities) {
            result = !SecurityUtil.isGranted(priv.name);
            
            if (requiresAll == result) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Returns true if this definition contains any editable properties.
     * 
     * @return True if editable properties are present.
     */
    public boolean hasEditableProperties() {
        for (PropertyInfo propInfo : properties) {
            if (propInfo.isEditable()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Sets the path of the resource containing the plugin definition. This is used to locate the
     * manifest entry from which version and source information can be extracted.
     * 
     * @param path Path of the resource containing this plugin definition.
     */
    public void setPath(String path) {
        if (path != null) {
            manifest = ManifestIterator.getInstance().findByPath(path);
        }
    }
    
    /**
     * Returns a value's default if the initial value is null or empty.
     * 
     * @param value The initial value.
     * @param manifestKey The manifest key from which to obtain the default value.
     * @return The initial value or, if it was null or empty, the default value.
     */
    private String getValueWithDefault(String value, String manifestKey) {
        if (StringUtils.isEmpty(value) && manifest != null) {
            value = manifest.getMainAttributes().getValue(manifestKey);
        }
        
        return value;
    }
    
    /**
     * Creates an instance of the element based on its definition. If a property source is
     * specified, the source is used to initialize properties on the newly created element.
     * 
     * @param parent Parent element (may be null).
     * @param propertySource Property source for initializing element properties (may be null).
     * @return Newly created element (may be null if access is restricted or plugin has been
     *         disabled).
     */
    public ElementBase createElement(ElementBase parent, IPropertyProvider propertySource) {
        try {
            ElementBase element = null;
            boolean deserializing = propertySource instanceof UILayout;
            
            if (isForbidden()) {
                log.info("Access to plugin " + getName() + " is restricted.");
            } else if (isDisabled()) {
                log.info("Plugin " + getName() + " has been disabled.");
            } else {
                Class<? extends ElementBase> clazz = getClazz();
                
                if (isInternal()) {
                    element = parent.getChild(clazz, null);
                } else {
                    element = clazz.newInstance();
                }
                
                if (element == null) {
                    UIException.raise("Failed to create UI element " + id + ".");
                }
                
                element.setDefinition(this);
                element.beforeInitialize(deserializing);
                
                if (propertySource != null) {
                    for (PropertyInfo propInfo : getProperties()) {
                        String key = propInfo.getId();
                        
                        if (propertySource.hasProperty(key)) {
                            String value = propertySource.getProperty(key);
                            propInfo.setPropertyValue(element, value);
                        }
                    }
                }
                
                if (parent != null) {
                    element.setParent(parent);
                }
                
                element.afterInitialize(deserializing);
            }
            
            return element;
            
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
}
