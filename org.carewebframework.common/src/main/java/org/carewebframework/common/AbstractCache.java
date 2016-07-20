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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract class for managing globally cached data. Subclasses must implement the fetch logic for
 * retrieving the data object into the cache.
 * 
 * @param <KEY> The class of the indexing key.
 * @param <VALUE> The class of the cached item.
 */
public abstract class AbstractCache<KEY, VALUE> implements Iterable<VALUE> {
    
    private static class CachedObject<VALUE> {
        
        private VALUE object;
        
        private RuntimeException exception;
        
        private ReentrantLock lock = new ReentrantLock();
        
        private CachedObject() {
            lock.lock();
        }
        
        void setObject(VALUE object) {
            this.object = object;
            removeLock();
        }
        
        void setException(RuntimeException exception) {
            this.exception = exception;
            removeLock();
        }
        
        VALUE getObject() {
            if (exception != null) {
                throw exception;
            }
            
            lock();
            unlock();
            return object;
        }
        
        private void removeLock() {
            ReentrantLock lock = this.lock;
            this.lock = null;
            lock.unlock();
        }
        
        private void lock() {
            if (lock != null) {
                lock.lock();
            }
        }
        
        private void unlock() {
            if (lock != null) {
                lock.unlock();
            }
        }
    }
    
    private final Map<KEY, CachedObject<VALUE>> map = new ConcurrentHashMap<>();
    
    /**
     * Logic to retrieve the data item from its primary store based on the provided key. The
     * returned item will be stored in the cache for future retrieval. Note that it is acceptable to
     * return a null item from this call.
     * 
     * @param key The key.
     * @return The fetched value.
     */
    protected abstract VALUE fetch(KEY key);
    
    /**
     * Get value for specified key. If not in cache, will call subclass's fetch method and load into
     * cache.
     * 
     * @param key The key.
     * @return The associated value.
     */
    public VALUE get(KEY key) {
        return isCached(key) ? map.get(key).getObject() : internalGet(key);
    }
    
    /**
     * Returns true if the item associated with the specified key is in the cache.
     * 
     * @param key The key.
     * @return True if associated item has been cached.
     */
    public boolean isCached(KEY key) {
        return map.containsKey(key);
    }
    
    /**
     * Internal, thread-safe method for loading result into cache.
     * 
     * @param key The key.
     * @return The associated value.
     */
    private VALUE internalGet(KEY key) {
        CachedObject<VALUE> cachedObject;
        boolean needsFetch;
        
        synchronized (map) {
            needsFetch = !map.containsKey(key);
            
            if (needsFetch) {
                cachedObject = new CachedObject<VALUE>();
                map.put(key, cachedObject);
            } else {
                cachedObject = map.get(key);
            }
        }
        
        if (needsFetch) {
            try {
                cachedObject.setObject(fetch(key));
            } catch (Throwable e) {
                RuntimeException e2 = MiscUtil.toUnchecked(e);
                cachedObject.setException(e2);
                throw e2;
            }
        }
        
        return cachedObject.getObject();
    }
    
    /**
     * Refresh the cache. Any existing entries in the cache will be re-fetched after it is cleared.
     */
    public void refresh() {
        synchronized (map) {
            Set<KEY> contents = new HashSet<>(map.keySet());
            map.clear();
            
            for (KEY key : contents) {
                internalGet(key);
            }
        }
    }
    
    /**
     * Iterate over value set.
     */
    @Override
    public Iterator<VALUE> iterator() {
        return new Iterator<VALUE>() {
            
            Iterator<CachedObject<VALUE>> iterator = map.values().iterator();
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public VALUE next() {
                return iterator.next().getObject();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }
    
    /**
     * Return number of entries.
     * 
     * @return Number of entries.
     */
    public int size() {
        return map.size();
    }
    
}
