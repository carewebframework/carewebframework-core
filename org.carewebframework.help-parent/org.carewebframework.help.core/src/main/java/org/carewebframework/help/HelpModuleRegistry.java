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

import org.carewebframework.api.spring.BeanRegistry;
import org.carewebframework.common.Localizer;

/**
 * Registry of all known help modules.
 */
public class HelpModuleRegistry extends BeanRegistry<String, HelpModule> {
    
    private static final HelpModuleRegistry instance = new HelpModuleRegistry();
    
    public static HelpModuleRegistry getInstance() {
        return instance;
    }
    
    private IHelpSearch service;
    
    /**
     * Enforce singleton instance.
     */
    private HelpModuleRegistry() {
        super(HelpModule.class);
    }
    
    /**
     * Make <code>get</code> locale-aware.
     */
    @Override
    public HelpModule get(String key) {
        key = HelpModule.getLocalizedId(key, Localizer.getDefaultLocale());
        
        while (!key.isEmpty()) {
            HelpModule helpModule = super.get(key);
            
            if (helpModule != null) {
                return helpModule;
            }
            
            int i = key.lastIndexOf('_');
            key = i < 0 ? "" : key.substring(0, i);
        }
        
        return null;
    }
    
    @Override
    protected String getKey(HelpModule helpModule) {
        return helpModule.getLocalizedId();
    }
    
    @Override
    public void register(HelpModule helpModule) {
        super.register(helpModule);
        
        if (service != null) {
            service.indexHelpModule(helpModule);
        }
    }
    
    public void setService(IHelpSearch service) {
        this.service = service;
    }
    
}
