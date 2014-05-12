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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps a map, providing the ability to control whether key values may be replaced.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the stored item.
 */
public class RegistryMap<KEY, VALUE> implements Map<KEY, VALUE> {
    
    private final Map<KEY, VALUE> map;
    
    private final boolean replaceDuplicates;
    
    /**
     * Defaults to concurrent hash map and replaceable keys.
     */
    public RegistryMap() {
        this(true);
    }
    
    /**
     * Wraps the specified map, allowing replaceable keys.
     * 
     * @param map Map to be wrapped. If null, a concurrent hash map is created and used.
     */
    public RegistryMap(Map<KEY, VALUE> map) {
        this(map, true);
    }
    
    /**
     * Uses concurrent hash map.
     * 
     * @param replaceDuplicates If false, an exception is thrown when attempting to store a
     *            duplicate entry. If true, the duplicate entry replaces the original.
     */
    public RegistryMap(boolean replaceDuplicates) {
        this(null, replaceDuplicates);
    }
    
    /**
     * @param map Map to be wrapped. If null, a concurrent hash map is created and used.
     * @param replaceDuplicates If false, an exception is thrown when attempting to store a
     *            duplicate entry. If true, the duplicate entry replaces the original.
     */
    public RegistryMap(Map<KEY, VALUE> map, boolean replaceDuplicates) {
        this.replaceDuplicates = replaceDuplicates;
        this.map = map == null ? new ConcurrentHashMap<KEY, VALUE>() : map;
    }
    
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    
    @Override
    public VALUE get(Object key) {
        return map.get(key);
    }
    
    @Override
    public VALUE put(KEY key, VALUE value) {
        VALUE oldValue = null;
        
        if (key != null) {
            oldValue = map.get(key);
            
            if (!replaceDuplicates && oldValue != null && !oldValue.equals(value)) {
                throw new IllegalArgumentException("Cannot modify existing entry with the key '" + key + "'.");
            }
            
            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        }
        
        return oldValue;
    }
    
    @Override
    public VALUE remove(Object key) {
        return map.remove(key);
    }
    
    @Override
    public void putAll(Map<? extends KEY, ? extends VALUE> m) {
        for (Entry<? extends KEY, ? extends VALUE> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void clear() {
        map.clear();
    }
    
    @Override
    public Set<KEY> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    @Override
    public Collection<VALUE> values() {
        return Collections.unmodifiableCollection(map.values());
    }
    
    @Override
    public Set<java.util.Map.Entry<KEY, VALUE>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }
    
}
