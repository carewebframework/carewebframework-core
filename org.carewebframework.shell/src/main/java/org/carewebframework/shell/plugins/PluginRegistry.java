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

import org.carewebframework.common.AbstractRegistry;

/**
 * Registry of all known plugins.
 */
public class PluginRegistry extends AbstractRegistry<String, PluginDefinition> {
    
    private final AbstractRegistry<Class<?>, PluginDefinition> classRegistry = new AbstractRegistry<Class<?>, PluginDefinition>() {
        
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
    public void register(PluginDefinition item) {
        super.register(item);
        classRegistry.register(item);
    }
    
    public boolean unregister(Class<?> clazz) {
        return unregister(get(clazz));
    }
    
    @Override
    public boolean unregister(PluginDefinition item) {
        classRegistry.unregister(item);
        return super.unregister(item);
    }
    
    public PluginDefinition get(Class<?> clazz) {
        return classRegistry.get(clazz);
    }
    
    @Override
    protected String getKey(PluginDefinition item) {
        return item.getId();
    }
    
}
