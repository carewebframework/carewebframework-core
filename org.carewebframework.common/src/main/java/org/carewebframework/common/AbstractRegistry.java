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
        this.map = new RegistryMap<>(map, duplicateAction);
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
     * @return The item that was removed, or null if not found.
     */
    public VALUE unregister(VALUE item) {
        return unregisterByKey(getKey(item));
    }
    
    /**
     * Removes an item from the registry using its key value.
     * 
     * @param key The key of the item to remove.
     * @return The item that was removed, or null if not found.
     */
    public VALUE unregisterByKey(KEY key) {
        return map.remove(key);
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
