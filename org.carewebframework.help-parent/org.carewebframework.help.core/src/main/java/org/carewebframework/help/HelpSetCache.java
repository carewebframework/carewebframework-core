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

import org.fujion.common.AbstractCache;

/**
 * Maintains a cache of all known help sets. This is a singleton class.
 */
public class HelpSetCache extends AbstractCache<HelpModule, IHelpSet> {
    
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
    protected IHelpSet fetch(HelpModule descriptor) {
        return HelpSetFactory.create(descriptor);
    }
    
    public IHelpSet getIfCached(HelpModule descriptor) {
        return isCached(descriptor) ? get(descriptor) : null;
    }
}
