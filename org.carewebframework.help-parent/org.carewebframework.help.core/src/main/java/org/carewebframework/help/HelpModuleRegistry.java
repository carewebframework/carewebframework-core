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
