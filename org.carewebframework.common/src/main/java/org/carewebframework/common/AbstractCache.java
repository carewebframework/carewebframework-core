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
        
        private ReentrantLock lock = new ReentrantLock();
        
        private CachedObject() {
            lock.lock();
        }
        
        public void setObject(VALUE object) {
            this.object = object;
            ReentrantLock lock = this.lock;
            this.lock = null;
            lock.unlock();
        }
        
        public VALUE getObject() {
            lock();
            unlock();
            return object;
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
        return isCached(key) ? map.get(key).object : internalGet(key);
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
        VALUE value = null;
        CachedObject<VALUE> cachedObject = null;
        
        synchronized (map) {
            if (!map.containsKey(key)) {
                cachedObject = new CachedObject<VALUE>();
                map.put(key, cachedObject);
            } else {
                value = map.get(key).getObject();
            }
        }
        
        if (cachedObject != null) {
            try {
                cachedObject.setObject(fetch(key));
            } catch (Throwable e) {
                cachedObject.setObject(null);
            }
        }
        
        return value;
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
