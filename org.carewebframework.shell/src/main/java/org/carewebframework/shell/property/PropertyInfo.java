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
package org.carewebframework.shell.property;

import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Holds information about a specific plug-in property.
 */
public class PropertyInfo {
    
    private String name;
    
    private String id;
    
    private String type = "text";
    
    private PropertyType propertyType;
    
    private String description;
    
    private String getter;
    
    private String setter;
    
    private boolean serializable = true;
    
    private boolean editable = true;
    
    private String dflt;
    
    private final Properties config = new Properties();
    
    public PropertyInfo() {
        super();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
        
        if (!StringUtils.isEmpty(id)) {
            String s = firstUpper(id);
            getter = getter == null ? "get" + s : getter;
            setter = setter == null ? "set" + s : setter;
        }
    }
    
    public String getGetter() {
        return getter;
    }
    
    public void setGetter(String getter) {
        this.getter = getter;
    }
    
    public String getSetter() {
        return setter;
    }
    
    public void setSetter(String setter) {
        this.setter = setter;
    }
    
    public PropertyType getPropertyType() {
        if (propertyType == null) {
            propertyType = PropertyTypeRegistry.getInstance().get(getType());
        }
        
        return propertyType;
    }
    
    public String getType() {
        if (!"enum".equals(type)) {
            return type;
        }
        
        String className = getConfigValue("class");
        return "enum:" + (className != null ? "class:" + className : "bean:" + getConfigValue("bean"));
    }
    
    public void setType(String type) {
        this.type = type;
        propertyType = null;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Properties getConfig() {
        return config;
    }
    
    public void setConfig(String config) {
        setConfig(config == null ? null : config.split("\\|"));
    }
    
    public void setConfig(Properties config) {
        this.config.clear();
        this.config.putAll(config);
    }
    
    private void setConfig(String[] config) {
        this.config.clear();
        
        if (config != null) {
            for (String param : config) {
                String[] pcs = param.split("\\=", 2);
                
                if (pcs.length == 2) {
                    this.config.put(pcs[0], pcs[1]);
                }
            }
        }
    }
    
    /**
     * Convert first character of a string to upper case.
     * 
     * @param value String to convert
     * @return Converted string
     */
    private String firstUpper(String value) {
        return value == null ? null : value.substring(0, 1).toUpperCase() + value.substring(1);
    }
    
    /**
     * Returns the property value for a specified object instance.
     * 
     * @param instance The object instance.
     * @return The object's property value.
     * @throws Exception Unspecified exception.
     */
    public Object getPropertyValue(Object instance) throws Exception {
        if (instance == null) {
            return dflt == null || !isSerializable() ? null : getPropertyType().getSerializer().deserialize(dflt);
        }
        
        if (instance instanceof IPropertyAccessor) {
            return ((IPropertyAccessor) instance).getPropertyValue(this);
        }
        
        Method method = PropertyUtil.findGetter(getter, instance, null);
        return method == null ? null : method.invoke(instance);
    }
    
    /**
     * Sets the property value for a specified object instance.
     * 
     * @param instance The object instance.
     * @param value The value to assign.
     * @throws Exception Unspecified exception.
     */
    public void setPropertyValue(Object instance, Object value) throws Exception {
        if (instance instanceof IPropertyAccessor) {
            ((IPropertyAccessor) instance).setPropertyValue(this, value);
            return;
        }
        
        Method method = null;
        
        try {
            method = PropertyUtil.findSetter(setter, instance, value == null ? null : value.getClass());
        } catch (Exception e) {
            if (value != null) {
                PropertySerializer<?> serializer = getPropertyType().getSerializer();
                value = value instanceof String ? serializer.deserialize((String) value) : serializer.serialize(value);
                method = PropertyUtil.findSetter(setter, instance, value.getClass());
            } else {
                throw e;
            }
        }
        
        if (method != null) {
            method.invoke(instance, value);
        }
    }
    
    /**
     * Returns whether the property is serializable.
     * 
     * @return True if the property is serializable.
     */
    public boolean isSerializable() {
        return serializable && getPropertyType().getSerializer() != null;
    }
    
    /**
     * Sets whether the object is serializable. Properties marked as not serializable will be
     * ignored during the serialization/deserialization process.
     * 
     * @param serializable True if the property is serializable.
     */
    public void setSerializable(boolean serializable) {
        this.serializable = serializable;
    }
    
    /**
     * Returns whether the property's value is editable.
     * 
     * @return True if the property is editable.
     */
    public boolean isEditable() {
        return editable;
    }
    
    /**
     * Sets whether the object is editable. Properties marked as not editable will be not be
     * presented by the property editor.
     * 
     * @param editable True if the property is editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    /**
     * This is a convenience method for returning a named configuration value.
     * 
     * @param key The configuration value's key.
     * @return Configuration value or null if not found.
     */
    public String getConfigValue(String key) {
        return config.getProperty(key);
    }
    
    /**
     * This is a convenience method for returning a named configuration value that is expected to be
     * an integer.
     * 
     * @param key The configuration value's key.
     * @param dflt Default value.
     * @return Configuration value as an integer or default value if not found or not a valid
     *         integer.
     */
    public Integer getConfigValueInt(String key, Integer dflt) {
        try {
            return Integer.parseInt(getConfigValue(key));
        } catch (Exception e) {
            return dflt;
        }
    }
    
    /**
     * This is a convenience method for returning a named configuration value that is expected to be
     * a double floating point number.
     * 
     * @param key The configuration value's key.
     * @param dflt Default value.
     * @return Configuration value as a double or default value if not found or not a valid double.
     */
    public Double getConfigValueDouble(String key, Double dflt) {
        try {
            return Double.parseDouble(getConfigValue(key));
        } catch (Exception e) {
            return dflt;
        }
    }
    
    /**
     * This is a convenience method for returning a named configuration value that is expected to be
     * a Boolean value.
     * 
     * @param key The configuration value's key.
     * @param dflt Default value.
     * @return Configuration value as a Boolean or default value if not found or not a valid
     *         Boolean.
     */
    public Boolean getConfigValueBoolean(String key, Boolean dflt) {
        try {
            String value = getConfigValue(key);
            return value == null ? dflt : Boolean.parseBoolean(value);
        } catch (Exception e) {
            return dflt;
        }
    }
    
    /**
     * This is a convenience method for returning a named configuration value that is expected to be
     * a list of array elements delimited by commas.
     * 
     * @param key The configuration value's key.
     * @return The array of values.
     */
    public String[] getConfigValueArray(String key) {
        return StringUtils.split(getConfigValue(key), ",");
    }
    
    /**
     * This is a convenience method for returning a named configuration value that is expected to be
     * a list of array elements.
     * 
     * @param key The configuration value's key.
     * @param delimiter The delimiter separating array elements.
     * @return The array of values.
     */
    public String[] getConfigValueArray(String key, String delimiter) {
        return StringUtils.split(getConfigValue(key), delimiter);
    }
    
    /**
     * Returns the default value for this property.
     * 
     * @return The default value.
     */
    public String getDefault() {
        return dflt;
    }
    
    /**
     * Sets the default value for this property.
     * 
     * @param dflt The default value.
     */
    public void setDefault(String dflt) {
        this.dflt = dflt;
    }
    
}
