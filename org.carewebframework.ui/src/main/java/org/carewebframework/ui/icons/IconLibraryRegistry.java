/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.icons;

import java.util.Collections;
import java.util.List;

import org.apache.commons.digester.SimpleRegexMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.AbstractRegistry;

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
     * @return
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
     * @param library Library name containing the icon (e.g., "silk").
     * @param iconName Name of the requested icon (e.g., "help*.png").
     * @param dimensions Dimensions of the requested icon (e.g., "16x*").
     * @return The icon path.
     */
    public List<String> getMatching(String library, final String iconName, String dimensions) {
        SimpleRegexMatcher matcher = new SimpleRegexMatcher();
        library = library == null ? getDefaultLibrary() : library;
        dimensions = dimensions == null ? defaultDimensions : dimensions;
        
        List<String> results = null;
        
        for (IIconLibrary lib : this) {
            if (matcher.match(lib.getId(), library)) {
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
