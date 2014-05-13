/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.alias;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.AbstractRegistry;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Global registry for aliases. Supports aliases for different alias types as defined by the
 * AliasType class. Aliases may be loaded from one or more property files and may be added
 * programmatically.
 */
public class AliasTypeRegistry extends AbstractRegistry<String, AliasType> implements ApplicationContextAware {
    
    private static final Log log = LogFactory.getLog(AliasTypeRegistry.class);
    
    private static final AliasTypeRegistry instance = new AliasTypeRegistry();
    
    private static final char PREFIX_DELIM = '^';
    
    private static final String PREFIX_DELIM_REGEX = "\\" + PREFIX_DELIM;
    
    private String propertyFile;
    
    private int fileCount;
    
    private int entryCount;
    
    /**
     * Returns reference to the alias registry.
     * 
     * @return Reference to the alias registry.
     */
    public static AliasTypeRegistry getInstance() {
        return instance;
    }
    
    /**
     * Convenience method for accessing alias type.
     * 
     * @param type Key associated with alias type.
     * @return The alias type (never null).
     */
    public static AliasType getType(String type) {
        return instance.get(type);
    }
    
    /**
     * Registers an alias for a key.
     * 
     * @param type Name of the alias type.
     * @param local Local name.
     * @param alias Alias for the local name. A null value removes any existing alias.
     */
    public static void register(String type, String local, String alias) {
        instance.get(type).register(local, alias);
    }
    
    /**
     * Enforce singleton instance.
     */
    private AliasTypeRegistry() {
        super();
    }
    
    /**
     * Sets the property file from which aliases are to be loaded. May be null or empty.
     * 
     * @param propertyFile Path of the property file.
     */
    public void setPropertyFile(String propertyFile) {
        this.propertyFile = propertyFile;
    }
    
    /**
     * Returns the AliasType given the key, creating and registering it if it does not already
     * exist.
     * 
     * @param key Unique key of alias type.
     * @return The alias type (never null).
     */
    @Override
    public AliasType get(String key) {
        key = key.toUpperCase();
        AliasType type = super.get(key);
        
        if (type == null) {
            register(type = new AliasType(key));
        }
        
        return type;
    }
    
    /**
     * Loads aliases defined in an external property file, if specified.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (StringUtils.isEmpty(propertyFile)) {
            return;
        }
        
        for (String pf : propertyFile.split("\\,")) {
            loadAliases(applicationContext, pf);
        }
        
        if (fileCount > 0) {
            log.info("Loaded " + entryCount + " aliases from " + fileCount + " files.");
        }
    }
    
    /**
     * Load aliases from a property file.
     * 
     * @param applicationContext
     * @param propertyFile
     */
    private void loadAliases(ApplicationContext applicationContext, String propertyFile) {
        if (propertyFile.isEmpty()) {
            return;
        }
        
        Resource[] resources;
        
        try {
            resources = applicationContext.getResources(propertyFile);
        } catch (IOException e) {
            log.error("Failed to locate alias property file: " + propertyFile, e);
            return;
        }
        
        for (Resource resource : resources) {
            if (!resource.exists()) {
                log.info("Did not find alias property file: " + resource.getFilename());
                continue;
            }
            
            InputStream is = null;
            
            try {
                is = resource.getInputStream();
                Properties props = new Properties();
                props.load(is);
                
                for (Entry<Object, Object> entry : props.entrySet()) {
                    try {
                        register((String) entry.getKey(), (String) entry.getValue());
                        entryCount++;
                    } catch (Exception e) {
                        log.error("Error registering alias for '" + entry.getKey() + "'.", e);
                    }
                }
                
                fileCount++;
            } catch (IOException e) {
                log.error("Failed to load alias property file: " + resource.getFilename(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
    
    /**
     * Registers an alias for a key prefixed with an alias type.
     * 
     * @param key Local name with alias type prefix.
     * @param alias Alias for the key. A null value removes any existing alias.
     */
    private void register(String key, String alias) {
        String[] pcs = key.split(PREFIX_DELIM_REGEX, 2);
        
        if (pcs.length != 2) {
            throw new IllegalArgumentException("Illegal key value: " + key);
        }
        
        register(pcs[0], pcs[1], alias);
    }
    
    @Override
    protected String getKey(AliasType item) {
        return item.getName();
    }
}
