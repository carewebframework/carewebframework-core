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
package org.carewebframework.api.property.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;

import org.carewebframework.api.property.IPropertyService;
import org.carewebframework.common.StrUtil;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Mock implementation of a property service.
 */
public class MockPropertyService implements IPropertyService {
    
    private static final String delim = "\\";
    
    private final Map<String, String> global_map = new HashMap<>();
    
    private final Map<String, String> local_map = new HashMap<>();
    
    public MockPropertyService() {
    }
    
    /**
     * Load property values from a property file. A property values are assumed local unless the
     * property name is prefixed with "global.". You may specify an instance name by appending to
     * the property name a "\" followed by the instance name. For example:
     * 
     * <pre>
     *  prop1 = value1
     *  global.prop2 = value2
     *  prop3\inst3 = value3
     *  global.prop4\inst4 = value4
     * </pre>
     * 
     * @param resource Property file resource.
     * @throws IOException IO exception.
     */
    public void addResource(Resource resource) throws IOException {
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        
        for (Entry<?, ?> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = StringEscapeUtils.unescapeJava((String) entry.getValue());
            boolean global = key.startsWith("global.");
            key = global ? key.substring(7) : key;
            
            if (!key.contains(delim)) {
                key += delim;
            }
            
            (global ? global_map : local_map).put(key, value);
        }
    }
    
    /**
     * Initializes the properties from a single resource.
     * 
     * @param resource Source of properties.
     * @throws IOException IO exception.
     */
    public void setResource(Resource resource) throws IOException {
        clear();
        addResource(resource);
    }
    
    /**
     * Initializes the properties from a multiple resources.
     * 
     * @param resources Sources of properties.
     * @throws IOException IO exception.
     */
    public void setResources(Resource[] resources) throws IOException {
        clear();
        
        for (Resource resource : resources) {
            addResource(resource);
        }
    }
    
    /**
     * Clear all properties.
     */
    public void clear() {
        global_map.clear();
        local_map.clear();
    }
    
    private String getKey(String propertyName, String instanceName) {
        return propertyName + delim + (instanceName == null ? "" : instanceName);
    }
    
    private String get(String propertyName, String instanceName) {
        String key = getKey(propertyName, instanceName);
        String value = local_map.get(key);
        return value != null ? value : global_map.get(key);
    }
    
    @Override
    public String getValue(String propertyName, String instanceName) {
        return get(propertyName, instanceName);
    }
    
    @Override
    public List<String> getValues(String propertyName, String instanceName) {
        return StrUtil.toList(get(propertyName, instanceName));
    }
    
    @Override
    public void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
        Map<String, String> map = asGlobal ? global_map : local_map;
        String key = getKey(propertyName, instanceName);
        
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }
    
    @Override
    public void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> value) {
        saveValue(propertyName, instanceName, asGlobal, StrUtil.fromList(value));
    }
    
    @Override
    public List<String> getInstances(String propertyName, boolean asGlobal) {
        List<String> list = new ArrayList<>();
        propertyName += delim;
        Map<String, String> map = asGlobal ? global_map : local_map;
        
        for (String key : map.keySet()) {
            if (key.startsWith(propertyName)) {
                list.add(key.split("\\\\")[1]);
            }
        }
        
        return list;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
}
