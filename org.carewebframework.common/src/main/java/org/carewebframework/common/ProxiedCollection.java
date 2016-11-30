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
import java.util.Iterator;

/**
 * Wraps a collection and delegates implementations for add and remove operations to an external
 * implementation.
 *
 * @param <T> The element type.
 */
public class ProxiedCollection<T> implements Collection<T> {
    
    public interface IProxiedCollectionOperations<T> {
        
        boolean add(T element, Collection<T> delegate);
        
        boolean remove(Object element, Collection<T> delegate);
        
        default void remove(T element, Iterator<T> iterator) {
            throw new UnsupportedOperationException();
        }
    }
    
    protected final Collection<T> delegate;
    
    private final IProxiedCollectionOperations<T> operations;
    
    public ProxiedCollection(Collection<T> delegate, IProxiedCollectionOperations<T> operations) {
        this.delegate = delegate;
        this.operations = operations;
    }
    
    @Override
    public int size() {
        return delegate.size();
    }
    
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }
    
    @Override
    public Iterator<T> iterator() {
        final Iterator<T> iterator = delegate.iterator();
        
        return new Iterator<T>() {
            
            private T current;
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public T next() {
                return current = iterator.next();
            }
            
            @Override
            public void remove() {
                operations.remove(current, iterator);
            }
        };
    }
    
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }
    
    @Override
    public <S> S[] toArray(S[] a) {
        return delegate.toArray(a);
    }
    
    @Override
    public final boolean add(T element) {
        return operations.add(element, delegate);
    }
    
    @Override
    public final boolean remove(Object element) {
        return operations.remove(element, delegate);
    }
    
    @Override
    public final boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }
    
    @Override
    public final boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        
        for (T element : c) {
            changed |= add(element);
        }
        
        return changed;
    }
    
    @Override
    public final boolean removeAll(Collection<?> c) {
        boolean changed = false;
        
        for (Object element : c) {
            changed |= remove(element);
        }
        
        return changed;
    }
    
    @Override
    public final boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<T> iter = iterator();
        
        while (iter.hasNext()) {
            T element = iter.next();
            
            if (!c.contains(element)) {
                iter.remove();
                changed = true;
            }
        }
        
        return changed;
    }
    
    @Override
    public final void clear() {
        Iterator<T> iter = iterator();
        
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }
    
}
