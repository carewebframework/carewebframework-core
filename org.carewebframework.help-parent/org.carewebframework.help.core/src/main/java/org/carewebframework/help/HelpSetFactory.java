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
package org.carewebframework.help;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.RegistryMap;
import org.carewebframework.common.RegistryMap.DuplicateAction;

/**
 * Factory class for creating help sets using registered implementations.
 */
public class HelpSetFactory {
    
    private static final Log log = LogFactory.getLog(HelpSetFactory.class);
    
    private static final HelpSetFactory instance = new HelpSetFactory();
    
    private final Map<String, Class<? extends IHelpSet>> map = new RegistryMap<>(
            DuplicateAction.ERROR);
            
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
     * @param module A help module descriptor.
     * @return An instantiation of the requested help set.
     */
    public static IHelpSet create(HelpModule module) {
        try {
            Class<? extends IHelpSet> clazz = instance.map.get(module.getFormat());
            
            if (clazz == null) {
                throw new Exception("Unsupported help format: " + module.getFormat());
            }
            
            Constructor<? extends IHelpSet> ctor = clazz.getConstructor(HelpModule.class);
            return ctor.newInstance(module);
        } catch (Exception e) {
            log.error("Error creating help set for " + module.getUrl(), e);
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpSetFactory() {
        super();
    }
    
}
