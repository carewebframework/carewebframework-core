/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for managing globally cached data. Subclasses must implement the fetch logic for
 * retrieving the data object into the cache.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the cached item.
 */
public abstract class AbstractGlobalCache<KEY, VALUE> extends AbstractGlobalMap<KEY, VALUE> {
    
    /**
     * Logic to retrieve the data item from its primary store based on the provided key. The
     * returned item will be stored in the cache for future retrieval. Note that it is acceptable to
     * return a null item from this call.
     * 
     * @param key
     * @return
     */
    protected abstract VALUE fetch(KEY key);
    
    /**
     * Get value for specified key. If not in cache, will call subclass's fetch method and load into
     * cache.
     * 
     * @param key The key.
     * @return The associated value.
     */
    @Override
    public VALUE get(KEY key) {
        return globalMap.containsKey(key) ? globalMap.get(key) : internalGet(key);
    }
    
    /**
     * Internal, thread-safe method for loading result into cache.
     * 
     * @param key The key.
     * @return The associated value.
     */
    private synchronized VALUE internalGet(KEY key) {
        VALUE value = globalMap.get(key);
        
        if (value == null) {
            value = fetch(key);
            
            if (value != null) {
                globalMap.put(key, value);
            }
        }
        
        return value;
    }
    
    /**
     * Refresh the cache. Any existing entries in the cache will be re-fetched after it is cleared.
     */
    public synchronized void refresh() {
        Set<KEY> contents = new HashSet<KEY>(globalMap.keySet());
        clear();
        
        for (KEY key : contents) {
            internalGet(key);
        }
    }
    
}
