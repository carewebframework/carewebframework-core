/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

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
        String realKey = prefix + key;
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
        PropertySourcesPlaceholderConfigurer cfg = applicationContext.getBean(PropertySourcesPlaceholderConfigurer.class);
        localPropertySource = cfg.getAppliedPropertySources()
                .get(PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME);
    }
}
