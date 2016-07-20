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
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;

/**
 * Base class for implementing weak collections.
 * 
 * @param <E> Class of element.
 */
public abstract class WeakCollectionBase<E> {
    
    private transient final ReferenceQueue<E> referenceQueue = new ReferenceQueue<>();
    
    /**
     * Remove any garbage-collected entries.
     */
    public void compact() {
        Reference<? extends E> ref = null;
        
        while ((ref = referenceQueue.poll()) != null) {
            removeReference(ref);
        }
    }
    
    protected abstract void removeReference(Reference<? extends E> reference);
    
    protected Iterator<E> getIterator(Iterable<WeakReference<E>> iterable) {
        compact();
        final Iterator<WeakReference<E>> iterator = iterable.iterator();
        
        return new Iterator<E>() {
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public E next() {
                return getReferent(iterator.next());
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    /**
     * Creates a weak reference for the specified element, associating it with the internal
     * reference queue.
     * 
     * @param element The element.
     * @return Weak reference for the element.
     */
    protected WeakReference<E> createWeakReference(E element) {
        return new WeakReference<E>(element, referenceQueue) {
            
            @Override
            public boolean equals(Object object) {
                if (this == object) {
                    return true;
                }
                
                E val1 = get();
                
                if (val1 != null && object instanceof WeakReference) {
                    return get().equals(((WeakReference<?>) object).get());
                }
                
                return false;
            }
        };
    }
    
    protected boolean contains(Collection<WeakReference<E>> c, Object item) {
        E element = cast(item);
        
        if (element == null) {
            return false;
        }
        
        compact();
        
        for (WeakReference<E> ref : c) {
            E ele2 = ref.get();
            
            if (element == ele2 || element.equals(ele2)) {
                return true;
            }
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    protected E cast(Object o) {
        try {
            return (E) o;
        } catch (Exception e) {
            return null;
        }
    }
    
    protected E getReferent(WeakReference<E> ref) {
        return ref == null ? null : ref.get();
    }
    
}
