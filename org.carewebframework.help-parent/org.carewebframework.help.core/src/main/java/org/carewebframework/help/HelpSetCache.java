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

import org.carewebframework.common.AbstractCache;

/**
 * Maintains a cache of all known help sets. This is a singleton class.
 */
public class HelpSetCache extends AbstractCache<HelpSetDescriptor, IHelpSet> {
    
    private static final HelpSetCache instance = new HelpSetCache();
    
    public static HelpSetCache getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpSetCache() {
        super();
    }
    
    @Override
    protected IHelpSet fetch(HelpSetDescriptor descriptor) {
        return HelpSetFactory.create(descriptor);
    }
    
}
