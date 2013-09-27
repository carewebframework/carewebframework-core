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

/**
 * Abstract base class for thread-safe registry of shared objects.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the registered item.
 */
public abstract class AbstractGlobalRegistry<KEY, VALUE> extends AbstractGlobalMap<KEY, VALUE> {
    
    private final boolean replaceDuplicates;
    
    protected AbstractGlobalRegistry() {
        this(true);
    }
    
    /**
     * @param replaceDuplicates If false, an exception is thrown when attempting to store a
     *            duplicate entry. If true, the duplicate entry replaces the original.
     */
    protected AbstractGlobalRegistry(boolean replaceDuplicates) {
        super();
        this.replaceDuplicates = replaceDuplicates;
    }
    
    /**
     * Returns the key to use to store the item.
     * 
     * @param item
     * @return
     */
    protected abstract KEY getKey(VALUE item);
    
    /**
     * Adds an item to the registry.
     * 
     * @param item
     */
    public void add(VALUE item) {
        KEY key = item == null ? null : getKey(item);
        
        if (key != null) {
            if (!replaceDuplicates) {
                checkDuplicate(key, item, globalMap);
            }
            globalMap.put(key, item);
        }
    }
    
    /**
     * Removes an item from the registry.
     * 
     * @param item The item to remove.
     * @return True if the item was successfully removed.
     */
    public boolean remove(VALUE item) {
        return removeByKey(getKey(item));
    }
    
    /**
     * Removes an item from the registry using its key value.
     * 
     * @param key The key of the item to remove.
     * @return True if the item was successfully removed.
     */
    public boolean removeByKey(KEY key) {
        boolean result = key != null && globalMap.containsKey(key);
        
        if (result) {
            globalMap.remove(key);
        }
        
        return result;
    }
    
}
