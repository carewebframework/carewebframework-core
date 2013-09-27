/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.carewebframework.api.AbstractGlobalRegistry;

/**
 * Registry of all known plugins.
 */
public class PluginRegistry extends AbstractGlobalRegistry<String, PluginDefinition> {
    
    private final AbstractGlobalRegistry<Class<?>, PluginDefinition> classRegistry = new AbstractGlobalRegistry<Class<?>, PluginDefinition>() {
        
        @Override
        protected Class<?> getKey(PluginDefinition item) {
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
        super(false);
    }
    
    @Override
    public void add(PluginDefinition item) {
        super.add(item);
        classRegistry.add(item);
    }
    
    public boolean remove(Class<?> clazz) {
        return remove(get(clazz));
    }
    
    @Override
    public boolean remove(PluginDefinition item) {
        classRegistry.remove(item);
        return super.remove(item);
    }
    
    public PluginDefinition get(Class<?> clazz) {
        return classRegistry.get(clazz);
    }
    
    @Override
    protected String getKey(PluginDefinition item) {
        return item.getId();
    }
    
}
