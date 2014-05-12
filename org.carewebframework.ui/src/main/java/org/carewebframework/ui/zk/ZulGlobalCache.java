/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.AbstractCache;

import org.zkoss.zk.ui.metainfo.PageDefinition;

/**
 * Supports caching of zul page definitions.
 */
public class ZulGlobalCache extends AbstractCache<String, PageDefinition> {
    
    private static final Log log = LogFactory.getLog(ZulGlobalCache.class);
    
    private static final ZulGlobalCache instance = new ZulGlobalCache();
    
    /**
     * Returns ZulGlobalCache instance
     * 
     * @return ZulGlobalCache
     */
    public static ZulGlobalCache getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private ZulGlobalCache() {
        super();
    }
    
    /**
     * @see org.carewebframework.common.AbstractCache#fetch(java.lang.Object)
     */
    @Override
    protected PageDefinition fetch(final String filename) {
        try {
            return ZKUtil.loadZulPageDefinition(filename);
        } catch (final Exception e) {
            log.warn("Exception occurred loading zul page definition from filename [" + StringUtils.trimToEmpty(filename)
                    + "], returning null PageDefinition.", e);
            return null;
        }
    }
    
}
