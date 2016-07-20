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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Implements the equivalent of an array list with weakly referenced elements.
 * 
 * @param <E> Class of element.
 */
public class WeakList<E> extends WeakCollectionBase<E>implements List<E> {
    
    private final List<WeakReference<E>> referenceList = new ArrayList<>();
    
    public WeakList() {
        super();
    }
    
    /**
     * Copy constructor
     * 
     * @param source Instance to be copied.
     */
    public WeakList(WeakList<E> source) {
        super();
        
        for (E element : source) {
            if (element != null) {
                add(element);
            }
        }
    }
    
    /**
     * Sets a weak reference to the specified element.
     */
    @Override
    public E set(int index, E element) {
        E previous = get(index);
        referenceList.set(index, createWeakReference(element));
        return previous;
    }
    
    /**
     * Removes the weak reference at the specified index.
     */
    @Override
    public E remove(int index) {
        return getReferent(referenceList.remove(index));
    }
    
    /**
     * Inserts a weak reference to the specified element at a specified index.
     */
    @Override
    public void add(int index, E element) {
        referenceList.add(index, createWeakReference(element));
    }
    
    /**
     * Returns the element referenced at the specified index. If the element has already been
     * garbage-collected, null is returned.
     */
    @Override
    public E get(int index) {
        return getReferent(referenceList.get(index));
    }
    
    /**
     * Returns the size of the underlying list.
     */
    @Override
    public int size() {
        compact();
        return referenceList.size();
    }
    
    @Override
    public boolean isEmpty() {
        compact();
        return referenceList.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return contains(referenceList, o);
    }
    
    @Override
    public Iterator<E> iterator() {
        return getIterator(referenceList);
    }
    
    @Override
    public Object[] toArray() {
        return toArray(new Object[] {});
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        compact();
        Object[] ary = referenceList.toArray(a);
        
        for (int i = 0; i < ary.length; i++) {
            ary[i] = getReferent((WeakReference<E>) ary[i]);
        }
        
        return (T[]) ary;
    }
    
    @Override
    public boolean add(E e) {
        return referenceList.add(createWeakReference(e));
    }
    
    @Override
    public boolean remove(Object o) {
        E element = cast(o);
        return element != null && referenceList.remove(createWeakReference(element));
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = false;
        
        for (E element : c) {
            result |= add(element);
        }
        
        return result;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean result = false;
        
        for (E element : c) {
            add(index++, element);
            result = true;
        }
        
        return result;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        
        for (Object element : c) {
            result |= remove(element);
        }
        
        return result;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        referenceList.clear();
    }
    
    @Override
    public int indexOf(Object o) {
        E element = cast(o);
        return element == null ? -1 : referenceList.indexOf(createWeakReference(element));
    }
    
    @Override
    public int lastIndexOf(Object o) {
        E element = cast(o);
        return element == null ? -1 : referenceList.lastIndexOf(createWeakReference(element));
    }
    
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void removeReference(Reference<? extends E> reference) {
        referenceList.remove(reference);
    }
}
