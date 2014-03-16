/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Global registry for aliases. Supports aliases for different alias types as defined by the
 * AliasType enum. Aliases may be loaded from one or more property files and may be added
 * programmatically.
 */
public class AliasRegistry extends AbstractGlobalMap<String, String> implements ApplicationContextAware {
    
    private static final Log log = LogFactory.getLog(AliasRegistry.class);
    
    private static final AliasRegistry instance = new AliasRegistry();
    
    public enum AliasType {
        AUTHORITY, PROPERTY
    };
    
    /**
     * Class for convenient access to aliases of a specific type.
     */
    public class AliasRegistryForType {
        
        private final AliasType type;
        
        private AliasRegistryForType(AliasType type) {
            this.type = type;
        }
        
        public String get(String key) {
            return AliasRegistry.this.get(type, key);
        }
        
        public boolean contains(String key) {
            return AliasRegistry.this.contains(type, key);
        }
        
        public void registerAlias(String key, String alias) {
            AliasRegistry.this.registerAlias(type, key, alias);
        }
    }
    
    private static final char PREFIX_DELIM = ':';
    
    private static final String PREFIX_DELIM_REGEX = "\\" + PREFIX_DELIM;
    
    private String propertyFile;
    
    /**
     * Returns reference to the alias registry.
     * 
     * @return Reference to the alias registry.
     */
    public static AliasRegistry getInstance() {
        return instance;
    }
    
    /**
     * Returns a reference to an accessor for the alias registry that is constrained to the
     * specified alias type.
     * 
     * @param type Alias type.
     * @return Type-specific accessor.
     */
    public static AliasRegistryForType getInstance(AliasType type) {
        return instance.aliasRegistryForType(type);
    }
    
    /**
     * Enforce singleton instance.
     */
    private AliasRegistry() {
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
     * Registers an alias for a key.
     * 
     * @param key Key name with alias type prefix.
     * @param alias Alias for the key. A null value removes any existing alias.
     */
    public void registerAlias(String key, String alias) {
        String[] pcs = key.split(PREFIX_DELIM_REGEX, 2);
        
        if (pcs.length != 2) {
            throw new IllegalArgumentException("Illegal key value: " + key);
        }
        
        AliasType type;
        
        try {
            type = AliasType.valueOf(pcs[0].toUpperCase());
        } catch (Throwable t) {
            throw new IllegalArgumentException("Illegal alias type: " + pcs[0]);
        }
        
        registerAlias(type, pcs[1], alias);
    }
    
    /**
     * Registers an alias for a key.
     * 
     * @param type Type of alias being registered.
     * @param key Key name.
     * @param alias Alias for the key. A null value removes any existing alias.
     */
    public void registerAlias(AliasType type, String key, String alias) {
        key = prefixedKey(type, key);
        
        if (alias == null) {
            globalMap.remove(key);
            return;
        }
        
        if (globalMap.containsKey(key)) {
            if (globalMap.get(key).equals(alias)) {
                return;
            }
            throw new IllegalArgumentException(type.name() + " " + key + " already has a registered alias.");
        }
        globalMap.put(key, alias);
    }
    
    /**
     * Returns true if the key exists for the given alias type.
     * 
     * @param type The alias type.
     * @param key Key name.
     * @return True if the key exists for the alias type.
     */
    public boolean contains(AliasType type, String key) {
        return contains(prefixedKey(type, key));
    }
    
    /**
     * Returns the alias for a given key and alias type.
     * 
     * @param type The alias type.
     * @param key Key name.
     * @return Alias for the key, or null if not found.
     */
    public String get(AliasType type, String key) {
        return get(prefixedKey(type, key));
    }
    
    /**
     * Returns the key name in prefixed form.
     * 
     * @param type Alias type.
     * @param key Key name.
     * @return Prefixed key name.
     */
    private String prefixedKey(AliasType type, String key) {
        return type.name() + PREFIX_DELIM + key;
    }
    
    /**
     * Returns an accessor object limited to the specified alias type.
     * 
     * @param type Alias type.
     * @return Accessor object.
     */
    private AliasRegistryForType aliasRegistryForType(AliasType type) {
        return new AliasRegistryForType(type);
    }
    
    /**
     * Loads aliases defined in an external property file, if specified.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (StringUtils.isEmpty(propertyFile)) {
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
                log.info("Did not find authority alias property file: " + resource.getFilename());
                continue;
            }
            
            InputStream is = null;
            
            try {
                is = resource.getInputStream();
                Properties props = new Properties();
                props.load(is);
                
                for (String prop : props.stringPropertyNames()) {
                    try {
                        registerAlias(prop, props.getProperty(prop));
                    } catch (Exception e) {
                        log.error("Error registering alias for '" + prop + "'.", e);
                    }
                }
                
            } catch (IOException e) {
                log.error("Failed to load alias property file: " + resource.getFilename(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
}
