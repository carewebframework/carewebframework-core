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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.carewebframework.common.RegistryMap.DuplicateAction;

/**
 * Abstract base class for thread-safe registry of shared objects.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the registered item.
 */
public abstract class AbstractRegistry<KEY, VALUE> implements Iterable<VALUE> {
    
    protected final Map<KEY, VALUE> map;
    
    protected AbstractRegistry() {
        this(null);
    }
    
    protected AbstractRegistry(DuplicateAction duplicateAction) {
        this(null, duplicateAction);
    }
    
    protected AbstractRegistry(Map<KEY, VALUE> map, DuplicateAction duplicateAction) {
        this.map = new RegistryMap<KEY, VALUE>(map, duplicateAction);
    }
    
    /**
     * Returns the key to use to store the item.
     * 
     * @param item Item whose key is sought.
     * @return Key for the item.
     */
    protected abstract KEY getKey(VALUE item);
    
    /**
     * Returns the value associated with the specified key.
     * 
     * @param key Key whose associated value is sought.
     * @return Value associated with the key.
     */
    public VALUE get(KEY key) {
        return map.get(key);
    }
    
    /**
     * Returns a read-only collection of all registry entries.
     * 
     * @return Collection of registry entries.
     */
    public Collection<VALUE> getAll() {
        return Collections.unmodifiableCollection(map.values());
    }
    
    /**
     * Adds an item to the registry.
     * 
     * @param item Item to add.
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
     * Remove all registry entries.
     */
    public void clear() {
        map.clear();
    }
    
    /**
     * Return number of entries.
     * 
     * @return Number of entries.
     */
    public int size() {
        return map.size();
    }
    
    /**
     * Iterate over value set.
     */
    @Override
    public Iterator<VALUE> iterator() {
        return map.values().iterator();
    }
    
}
