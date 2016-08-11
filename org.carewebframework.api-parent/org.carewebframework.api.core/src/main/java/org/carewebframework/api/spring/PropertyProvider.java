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
package org.carewebframework.api.spring;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.property.IPropertyProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * Property provider that retrieves its data from Spring properties. If a prefix is supplied, it is
 * applied to the key before retrieving data.
 */
public class PropertyProvider implements IPropertyProvider, ApplicationContextAware {
    
    private final String prefix;
    
    private Environment environment;
    
    private PropertySource<?> localPropertySource;
    
    public PropertyProvider() {
        this(null, null);
    }
    
    public PropertyProvider(String prefix) {
        this(prefix, null);
    }
    
    public PropertyProvider(ApplicationContext applicationContext) {
        this(null, applicationContext);
    }
    
    public PropertyProvider(String prefix, ApplicationContext applicationContext) {
        this.prefix = prefix == null ? "" : prefix.endsWith(".") ? prefix : prefix + ".";
        
        if (applicationContext != null) {
            setApplicationContext(applicationContext);
        }
    }
    
    @Override
    public String getProperty(String key) {
        String realKey = getRealKey(key);
        String value = environment == null ? null : environment.getProperty(realKey);
        return value != null ? value
                : localPropertySource == null ? null : (String) localPropertySource.getProperty(realKey);
    }
    
    @Override
    public boolean hasProperty(String key) {
        return getProperty(key) != null;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        environment = applicationContext.getEnvironment();
        PropertySourcesPlaceholderConfigurer cfg = applicationContext.getBean(
            "org.springframework.context.support.PropertySourcesPlaceholderConfigurer#0",
            PropertySourcesPlaceholderConfigurer.class);
        localPropertySource = cfg.getAppliedPropertySources()
                .get(PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME);
    }
    
    protected String getRealKey(String key) {
        return prefix + key;
    }
    
    public Map<String, String> toMap(Iterable<String> keys) {
        Map<String, String> map = new HashMap<>();
        
        for (String key : keys) {
            String value = getProperty(key);
            
            if (value != null) {
                map.put(getRealKey(key), value);
            }
        }
        
        return map;
    }
}
