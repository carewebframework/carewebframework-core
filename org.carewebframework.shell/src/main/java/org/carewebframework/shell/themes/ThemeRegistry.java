/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.themes;

import org.carewebframework.api.AbstractGlobalRegistry;

/**
 * Registry for theme definitions.
 */
public class ThemeRegistry extends AbstractGlobalRegistry<String, ThemeDefinition> {
    
    private static final ThemeRegistry instance = new ThemeRegistry();
    
    public static ThemeRegistry getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private ThemeRegistry() {
        super();
    }
    
    @Override
    protected String getKey(ThemeDefinition item) {
        return item.getId();
    }
    
}
