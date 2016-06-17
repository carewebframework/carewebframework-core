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

import org.carewebframework.api.spring.BeanRegistry;
import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.shell.layout.UIElementBase;

/**
 * Registry of all known plugins.
 */
public class PluginRegistry extends BeanRegistry<String, PluginDefinition> {
    
    private final AbstractRegistry<Class<? extends UIElementBase>, PluginDefinition> classRegistry = new AbstractRegistry<Class<? extends UIElementBase>, PluginDefinition>() {
        
        @Override
        protected Class<? extends UIElementBase> getKey(PluginDefinition item) {
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
    
    public PluginDefinition unregister(Class<? extends UIElementBase> clazz) {
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
    public PluginDefinition get(Class<? extends UIElementBase> clazz) {
        return classRegistry.get(clazz);
    }
    
    @Override
    protected String getKey(PluginDefinition item) {
        return item.getId();
    }
    
}
