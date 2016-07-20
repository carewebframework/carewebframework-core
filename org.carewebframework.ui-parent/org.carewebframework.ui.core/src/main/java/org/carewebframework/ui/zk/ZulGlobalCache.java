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
    protected PageDefinition fetch(String filename) {
        try {
            return ZKUtil.loadZulPageDefinition(filename);
        } catch (Exception e) {
            log.warn("Exception occurred loading zul page definition from filename [" + StringUtils.trimToEmpty(filename)
                    + "], returning null PageDefinition.", e);
            return null;
        }
    }
    
}
