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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the equivalent of an array list with weakly referenced elements.
 * 
 * @param <E> Class of element.
 */
public class WeakArrayList<E> extends AbstractList<E> {
    
    private transient final ReferenceQueue<E> referenceQueue = new ReferenceQueue<>();
    
    private final List<WeakReference<E>> referenceList = new ArrayList<>();
    
    public WeakArrayList() {
        super();
    }
    
    /**
     * Copy constructor
     * 
     * @param source Instance to be copied.
     */
    public WeakArrayList(WeakArrayList<E> source) {
        super();
        
        for (E element : source) {
            if (element != null) {
                add(element);
            }
        }
    }
    
    /**
     * Remove any garbage-collected entries.
     */
    public void compact() {
        Reference<? extends E> ref = null;
        
        while ((ref = referenceQueue.poll()) != null) {
            referenceList.remove(ref);
        }
    }
    
    /**
     * Creates a weak reference for the specified element, associating it with the internal
     * reference queue.
     * 
     * @param element The element.
     * @return Weak reference for the element.
     */
    private WeakReference<E> createWeakReference(E element) {
        return new WeakReference<>(element, referenceQueue);
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
        return referenceList.remove(index).get();
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
        return referenceList.get(index).get();
    }
    
    /**
     * Returns the size of the underlying list.
     */
    @Override
    public int size() {
        return referenceList.size();
    }
}
