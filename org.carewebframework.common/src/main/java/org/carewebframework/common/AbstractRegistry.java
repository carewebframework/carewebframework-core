/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract base class for thread-safe registry of shared objects.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the registered item.
 */
public abstract class AbstractRegistry<KEY, VALUE> implements Iterable<VALUE> {
    
    private final Map<KEY, VALUE> map;
    
    protected AbstractRegistry() {
        this(true);
    }
    
    protected AbstractRegistry(boolean replaceDuplicates) {
        map = new RegistryMap<KEY, VALUE>(replaceDuplicates);
    }
    
    /**
     * Returns the key to use to store the item.
     * 
     * @param item
     * @return
     */
    protected abstract KEY getKey(VALUE item);
    
    /**
     * Returns the value associated with the specified key.
     * 
     * @param key
     * @return
     */
    public VALUE get(KEY key) {
        return map.get(key);
    }
    
    /**
     * Adds an item to the registry.
     * 
     * @param item
     */
    public void register(VALUE item) {
        if (item != null) {
            map.put(getKey(item), item);
        }
    }
    
    /**
     * Removes an item from the registry.
     * 
     * @param item The item to remove.
     * @return True if the item was successfully removed.
     */
    public boolean unregister(VALUE item) {
        return unregisterByKey(getKey(item));
    }
    
    /**
     * Removes an item from the registry using its key value.
     * 
     * @param key The key of the item to remove.
     * @return True if the item was successfully removed.
     */
    public boolean unregisterByKey(KEY key) {
        return map.remove(key) != null;
    }
    
    /**
     * Iterate over value set.
     */
    @Override
    public Iterator<VALUE> iterator() {
        return map.values().iterator();
    }
    
}
