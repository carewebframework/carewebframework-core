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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implements the equivalent of a map with weakly referenced values. Note: this differs from a
 * WeakHashMap which uses weakly referenced keys.
 * 
 * @param <K> Class of key.
 * @param <E> Class of element.
 */
public class WeakMap<K, E> extends WeakCollectionBase<E>implements Map<K, E> {
    
    private final Map<K, WeakReference<E>> referenceMap = new HashMap<>();
    
    public WeakMap() {
        super();
    }
    
    /**
     * Copy constructor
     * 
     * @param source Instance to be copied.
     */
    public WeakMap(WeakMap<K, E> source) {
        super();
        
        for (Entry<K, E> entry : source.entrySet()) {
            if (entry.getValue() != null) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Returns the size of the underlying list.
     */
    @Override
    public int size() {
        compact();
        return referenceMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        compact();
        return referenceMap.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        compact();
        return referenceMap.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        compact();
        return contains(referenceMap.values(), value);
    }
    
    @Override
    public E get(Object key) {
        return getReferent(referenceMap.get(key));
    }
    
    @Override
    public E put(K key, E value) {
        return getReferent(referenceMap.put(key, createWeakReference(value)));
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends E> map) {
        for (Entry<? extends K, ? extends E> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public Set<Entry<K, E>> entrySet() {
        compact();
        
        return new AbstractSet<Entry<K, E>>() {
            
            @Override
            public Iterator<Entry<K, E>> iterator() {
                return new Iterator<Entry<K, E>>() {
                    
                    Iterator<Entry<K, WeakReference<E>>> iterator = referenceMap.entrySet().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    
                    @Override
                    public Entry<K, E> next() {
                        final Entry<K, WeakReference<E>> entry = iterator.next();
                        
                        return new Entry<K, E>() {
                            
                            @Override
                            public K getKey() {
                                return entry.getKey();
                            }
                            
                            @Override
                            public E getValue() {
                                return getReferent(entry.getValue());
                            }
                            
                            @Override
                            public E setValue(E value) {
                                return getReferent(entry.setValue(createWeakReference(value)));
                            }
                            
                        };
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            
            @Override
            public int size() {
                return referenceMap.size();
            }
            
        };
    }
    
    @Override
    public E remove(Object key) {
        return getReferent(referenceMap.remove(key));
    }
    
    @Override
    public void clear() {
        compact();
        referenceMap.clear();
    }
    
    @Override
    public Set<K> keySet() {
        compact();
        return referenceMap.keySet();
    }
    
    @Override
    public Collection<E> values() {
        compact();
        
        return new AbstractCollection<E>() {
            
            @Override
            public Iterator<E> iterator() {
                return getIterator(referenceMap.values());
            }
            
            @Override
            public int size() {
                return referenceMap.size();
            }
            
        };
    }
    
    @Override
    protected void removeReference(Reference<? extends E> reference) {
        referenceMap.values().remove(reference);
    }
    
}
