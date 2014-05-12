/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.RegistryMap;

/**
 * Factory class for creating help sets using registered implementations.
 */
public class HelpSetFactory {
    
    private static final Log log = LogFactory.getLog(HelpSetFactory.class);
    
    private static final HelpSetFactory instance = new HelpSetFactory();
    
    private final Map<String, Class<? extends IHelpSet>> map = new RegistryMap<String, Class<? extends IHelpSet>>(false);
    
    public static HelpSetFactory getInstance() {
        return instance;
    }
    
    /**
     * Register an implementation for one or more help formats.
     * 
     * @param clazz Implementation class.
     * @param formats Supported help formats, separated by commas.
     * @return Returns the implementation class.
     */
    public static Class<? extends IHelpSet> register(Class<? extends IHelpSet> clazz, String formats) {
        for (String type : formats.split("\\,")) {
            instance.map.put(type, clazz);
        }
        
        return clazz;
    }
    
    /**
     * Creates a help set.
     * 
     * @param format Format of the help set.
     * @param url Location of the help set.
     * @return An instantiation of the requested help set.
     */
    public static IHelpSet create(String format, String url) {
        try {
            Class<? extends IHelpSet> clazz = instance.map.get(format);
            
            if (clazz == null) {
                throw new Exception("Unsupported help format: " + format);
            }
            
            Constructor<? extends IHelpSet> ctor = clazz.getConstructor(String.class);
            return ctor.newInstance(url);
        } catch (Exception e) {
            log.error("Error creating help set for " + url, e);
            return null;
        }
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpSetFactory() {
        super();
    }
    
}
