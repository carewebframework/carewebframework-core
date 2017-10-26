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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
                        try (InputStream is = resource.getInputStream()) {
                            properties.load(is);
                        } catch (IOException e) {
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
