/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.StrUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import org.zkoss.util.Maps;
import org.zkoss.util.resource.Labels;
import org.zkoss.zel.impl.util.Validation;

/**
 * Class to find and register label resources. Searches for all files in class path matching the
 * pattern "zk-label*.properties" (and "i3-label*.properties" for backward compatibility) located at
 * or below the "web" folder. For each unique path, creates and registers a label locator for that
 * path.
 */
public class LabelFinder {
    
    /**
     * Class that encapsulates the label locator logic for a specific path.
     */
    private static class LabelLocator implements org.zkoss.util.resource.LabelLocator {
        
        private final List<URL> labelFiles = new ArrayList<URL>();
        
        /**
         * Creates a label locator.
         */
        public LabelLocator() {
        }
        
        /**
         * Adds the URL of a label property file to this locator.
         * 
         * @param url URL of the label property file.
         */
        public void addUrl(URL url) {
            labelFiles.add(url);
        }
        
        /**
         * Return a URL that matches the requested file name, or null if one does not exist.
         * 
         * @param fileName A file name.
         * @return The URL matching the file name, or null if none found.
         */
        private URL findUrl(String fileName) {
            for (URL url : labelFiles) {
                if (url.getPath().endsWith(fileName)) {
                    return url;
                }
            }
            
            return null;
        }
        
        /**
         * Called by ZK to locate a label resource for the specified locale. Note that ZK will first
         * attempt to find a label resource for the most specific locale. Failing that it will
         * repeat this call with less specific locales if applicable. Finally, failing all attempts,
         * it will make this call with a null locale.
         * 
         * @param locale The locale of the label resource being sought.
         * @return Returns the URL of the requested resource if it exists, or null otherwise.
         */
        @Override
        public URL locate(Locale locale) throws Exception {
            return findUrl("-label" + (locale == null ? "" : "_" + locale) + ".properties");
        }
    }
    
    private static final Log log = LogFactory.getLog(LabelFinder.class);
    
    private final boolean validate;
    
    /**
     * Sets the resolver to be used to resolve label references.
     */
    public LabelFinder() {
        this.validate = log.isWarnEnabled();
        StrUtil.setMessageSource(new LabelResolver());
    };
    
    /**
     * Searches the "web" folder and its subfolders in all class paths for label resources, creating
     * and registering label locators for each unique path.
     * 
     * @param appContext Root application context.
     */
    public void init(ApplicationContext appContext) {
        if (log.isInfoEnabled()) {
            log.info("Searching for label resources in class path...");
        }
        
        Map<String, LabelLocator> labelLocators = new HashMap<String, LabelLocator>();
        findLabelResources(appContext, "i3", labelLocators);
        findLabelResources(appContext, "zk", labelLocators);
        
        if (log.isInfoEnabled()) {
            log.info("Found " + labelLocators.size() + " label resources.");
        }
    }
    
    private void findLabelResources(ApplicationContext appContext, String prefix, Map<String, LabelLocator> labelLocators) {
        findLabelResources(appContext, "classpath*:/web/**/", prefix, labelLocators);
        findLabelResources(appContext, "/WEB-INF/", prefix, labelLocators);
    }
    
    private void findLabelResources(ApplicationContext appContext, String root, String prefix,
                                    Map<String, LabelLocator> labelLocators) {
        try {
            Resource[] resources = appContext.getResources(root + prefix + "-label*.properties");
            
            for (Resource resource : resources) {
                URL url = resource.getURL();
                String path = url.getPath();
                path = path.substring(path.lastIndexOf('!') + 1, path.lastIndexOf('/'));
                LabelLocator labelLocator = labelLocators.get(path);
                
                if (labelLocator == null) {
                    labelLocator = new LabelLocator();
                    labelLocators.put(path, labelLocator);
                    Labels.register(labelLocator);
                    
                    if (log.isInfoEnabled()) {
                        log.info("Label resource(s) found at '" + path + "'.");
                    }
                }
                
                labelLocator.addUrl(url);
                
                if (validate) {
                    validate(resource);
                }
            }
        } catch (IOException e) {
            log.error("Error searching for labels: " + e.getMessage());
        }
        
    }
    
    /**
     * Validates entries in a label resource.
     * 
     * @param resource A label resource.
     */
    private void validate(Resource resource) {
        InputStream is = null;
        
        try {
            Map<String, String> map = new HashMap<String, String>();
            is = resource.getInputStream();
            Maps.load(map, is);
            
            for (String key : map.keySet()) {
                for (String pc : key.split("\\.")) {
                    if (!Validation.isIdentifier(pc)) {
                        throw new RuntimeException("Label resource " + resource + " contains an invalid identifier: " + pc
                                + " in " + key);
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
    
}
