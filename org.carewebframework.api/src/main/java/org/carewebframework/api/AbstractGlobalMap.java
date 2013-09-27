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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for implementing indexed global registries and data caches. The essential difference
 * between registry and cache implementations is that items must be explicitly stored into a
 * registry whereas a cache implementation provides the logic for retrieving requested items that
 * are not currently in the cache.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the stored item.
 */
public class AbstractGlobalMap<KEY, VALUE> implements Iterable<VALUE> {
    
    protected final Map<KEY, VALUE> globalMap = new ConcurrentHashMap<KEY, VALUE>();
    
    /**
     * Checks the specified map for a duplicate entry, throwing an exception if found.
     * 
     * @param key Key to check.
     * @param value Value to check.
     * @param map Map to check.
     */
    protected static void checkDuplicate(Object key, Object value, Map<?, ?> map) {
        if (map.containsKey(key) && !map.get(key).equals(value)) {
            throw new RuntimeException("A registry entry with the key '" + key + "' already exists.");
        }
    }
    
    /**
     * Returns the item associated with the specified key, or null if not found.
     * 
     * @param key The key.
     * @return The associated item or null if not found.
     */
    public VALUE get(KEY key) {
        return globalMap.get(key);
    }
    
    /**
     * Clear the cache.
     */
    public synchronized void clear() {
        globalMap.clear();
    }
    
    /**
     * Returns the size of the map.
     * 
     * @return Number of map entries.
     */
    public int size() {
        return globalMap.size();
    }
    
    /**
     * Returns true if the map is empty.
     * 
     * @return True if the map is empty.
     */
    public boolean isEmpty() {
        return globalMap.isEmpty();
    }
    
    /**
     * Returns true if the map contains the specified key.
     * 
     * @param key Key of interest.
     * @return True if the key is present.
     */
    public boolean contains(KEY key) {
        return globalMap.containsKey(key);
    }
    
    /**
     * Each subclass supports the ability to iterate over its contents.
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<VALUE> iterator() {
        return globalMap.values().iterator();
    }
}
