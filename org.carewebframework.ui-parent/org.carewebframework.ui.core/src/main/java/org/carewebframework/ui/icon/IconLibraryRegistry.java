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
package org.carewebframework.ui.icon;

import java.util.Collections;
import java.util.List;

import org.apache.commons.digester.SimpleRegexMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.fujion.common.AbstractRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Registry for icon libraries. Will automatically registry icon library beans instantiated in the
 * root container.
 */
public class IconLibraryRegistry extends AbstractRegistry<String, IIconLibrary> implements BeanPostProcessor {
    
    private static final Log log = LogFactory.getLog(IconLibraryRegistry.class);
    
    private static final IconLibraryRegistry instance = new IconLibraryRegistry();
    
    private String defaultLibrary;
    
    private String defaultDimensions;
    
    public static IconLibraryRegistry init(String defaultLibrary, String defaultDimensions) {
        instance.defaultLibrary = defaultLibrary;
        instance.defaultDimensions = defaultDimensions;
        return instance;
    }
    
    public static IconLibraryRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private IconLibraryRegistry() {
        super();
    }
    
    @Override
    public IIconLibrary get(String library) {
        return super.get(library == null ? getDefaultLibrary() : library);
    }
    
    @Override
    protected String getKey(IIconLibrary item) {
        return item.getId();
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IIconLibrary) {
            IIconLibrary lib = (IIconLibrary) bean;
            register(lib);
            log.info("Registered icon library: " + lib.getId());
        }
        
        return bean;
    }
    
    /**
     * Returns the default icon library. If none has been explicitly set, assumes the first
     * registered library to be default.
     * 
     * @return The default icon library.
     */
    public String getDefaultLibrary() {
        if (defaultLibrary == null || defaultLibrary.isEmpty()) {
            defaultLibrary = iterator().next().getId();
        }
        
        return defaultLibrary;
    }
    
    /**
     * Returns the paths to matching icon resources given name, dimensions, and library name, any
     * one of which may contain wildcard characters.
     * 
     * @param library Library name containing the icon (e.g., "silk"). If null, all libraries are
     *            searched.
     * @param iconName Name of the requested icon (e.g., "help*.png").
     * @param dimensions Dimensions of the requested icon (e.g., "16x*").
     * @return The icon path.
     */
    public List<String> getMatching(String library, String iconName, String dimensions) {
        SimpleRegexMatcher matcher = new SimpleRegexMatcher();
        dimensions = dimensions == null ? defaultDimensions : dimensions;
        
        List<String> results = null;
        
        for (IIconLibrary lib : this) {
            if (library == null || matcher.match(lib.getId(), library)) {
                List<String> urls = lib.getMatching(iconName, dimensions);
                
                if (results == null) {
                    results = urls;
                } else {
                    results.addAll(urls);
                }
            }
        }
        
        return results == null ? Collections.<String> emptyList() : results;
    }
}
