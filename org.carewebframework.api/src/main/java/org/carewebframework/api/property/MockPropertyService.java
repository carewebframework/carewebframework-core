/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;

import org.carewebframework.common.StrUtil;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Mock implementation of a property service.
 */
public class MockPropertyService implements IPropertyService {
    
    private static final String delim = "\\";
    
    private final Map<String, String> global_map = new HashMap<String, String>();
    
    private final Map<String, String> local_map = new HashMap<String, String>();
    
    public MockPropertyService() {
    }
    
    /**
     * Constructor to allow initialization of property values from a property file. A property
     * values are assumed local unless the property name is prefixed with "global.". You may specify
     * an instance name by appending to the property name a "\" followed by the instance name. For
     * example:
     * 
     * <pre>
     *  prop1 = value1
     *  global.prop2 = value2
     *  prop3\inst3 = value3
     *  global.prop4\inst4 = value4
     * </pre>
     * 
     * @param resource
     * @throws IOException
     */
    public MockPropertyService(Resource resource) throws IOException {
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
        (asGlobal ? global_map : local_map).put(getKey(propertyName, instanceName), value);
    }
    
    @Override
    public void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> value) {
        saveValue(propertyName, instanceName, asGlobal, StrUtil.fromList(value));
    }
    
    @Override
    public List<String> getInstances(String propertyName, boolean asGlobal) {
        List<String> list = new ArrayList<String>();
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
