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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * Exposes local properties as a Spring property source.
 */
public class LocalPropertySource extends PropertySource<Object> {
    
    private static final Log log = LogFactory.getLog(LocalPropertySource.class);
    
    private final ApplicationContext appContext;
    
    private String[] sources;
    
    private Properties properties;
    
    public LocalPropertySource(ApplicationContext appContext) {
        super("Local Properties");
        this.appContext = appContext;
    }
    
    /**
     * Returns a property value from the underlying data store.
     */
    @Override
    public String getProperty(String name) {
        loadProperties();
        return properties == null ? null : properties.getProperty(name);
    }
    
    private void loadProperties() {
        if (sources != null && properties == null) {
            properties = new Properties();
            
            for (String source : sources) {
                try {
                    for (Resource resource : appContext.getResources(source)) {
                        InputStream is = null;
                        try {
                            is = resource.getInputStream();
                            properties.load(is);
                        } catch (IOException e) {
                            IOUtils.closeQuietly(is);
                            log.error("Error loading properties from " + resource.getFilename(), e);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error load property resources.", e);
                }
            }
        }
    }
    
    public void setSources(String[] sources) {
        this.sources = sources;
        properties = null;
    }
}
