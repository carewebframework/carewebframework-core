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
package org.carewebframework.shell.plugins;

import org.carewebframework.api.spring.BeanRegistry;
import org.fujion.common.AbstractRegistry;
import org.carewebframework.shell.elements.ElementBase;

/**
 * Registry of all known plugins.
 */
public class PluginRegistry extends BeanRegistry<String, PluginDefinition> {
    
    private final AbstractRegistry<Class<? extends ElementBase>, PluginDefinition> classRegistry = new AbstractRegistry<Class<? extends ElementBase>, PluginDefinition>() {
        
        @Override
        protected Class<? extends ElementBase> getKey(PluginDefinition item) {
            return item.getClazz();
        }
    };
    
    private static final PluginRegistry instance = new PluginRegistry();
    
    public static PluginRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private PluginRegistry() {
        super(PluginDefinition.class);
    }
    
    @Override
    public void register(PluginDefinition item) {
        super.register(item);
        classRegistry.register(item);
    }
    
    public PluginDefinition unregister(Class<? extends ElementBase> clazz) {
        return unregister(get(clazz));
    }
    
    @Override
    public PluginDefinition unregisterByKey(String id) {
        return classRegistry.unregister(super.unregisterByKey(id));
    }
    
    /**
     * Returns the plugin definition for a given UI element.
     * 
     * @param clazz The class of the UI element.
     * @return The associated plugin definition, or null if not found.
     */
    public PluginDefinition get(Class<? extends ElementBase> clazz) {
        return classRegistry.get(clazz);
    }
    
    @Override
    protected String getKey(PluginDefinition item) {
        return item.getId();
    }
    
}
