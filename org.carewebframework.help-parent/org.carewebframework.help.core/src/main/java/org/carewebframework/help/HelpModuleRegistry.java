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

/**
 * Registry of all known help modules.
 */
public class HelpModuleRegistry extends BeanRegistry<String, HelpModule> {
    
    private static final HelpModuleRegistry instance = new HelpModuleRegistry();
    
    public static HelpModuleRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpModuleRegistry() {
        super(HelpModule.class);
    }
    
    @Override
    protected String getKey(HelpModule item) {
        return item.getId();
    }
}
