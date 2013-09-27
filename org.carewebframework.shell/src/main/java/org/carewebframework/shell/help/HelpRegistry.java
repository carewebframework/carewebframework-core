/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.help;

import org.carewebframework.api.AbstractGlobalRegistry;

/**
 * Registry of all known help modules.
 */
public class HelpRegistry extends AbstractGlobalRegistry<String, HelpDefinition> {
    
    private static final HelpRegistry instance = new HelpRegistry();
    
    public static HelpRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpRegistry() {
        super();
    }
    
    @Override
    protected String getKey(HelpDefinition item) {
        return item.getId();
    }
}
