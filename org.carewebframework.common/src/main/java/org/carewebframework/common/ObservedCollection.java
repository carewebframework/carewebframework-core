package org.carewebframework.common;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wraps a collection and notifies a listener of all changes.
 *
 * @param <T> The element type.
 */
public class ObservedCollection<T> implements Collection<T> {
    
    public interface IObservedCollectionListener<T> {
        
        void beforeAddElement(T element);
        
        void afterAddElement(T element);
        
        void beforeRemoveElement(T element);
        
        void afterRemoveElement(T element);
    }
    
    private final Collection<T> delegate;
    
    private final IObservedCollectionListener<T> listener;
    
    public ObservedCollection(Collection<T> delegate, IObservedCollectionListener<T> listener) {
        this.delegate = delegate;
        this.listener = listener;
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
                listener.beforeRemoveElement(current);
                iterator.remove();
                listener.afterRemoveElement(current);
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
    public boolean add(T e) {
        listener.beforeAddElement(e);
        
        if (delegate.add(e)) {
            listener.afterAddElement(e);
            return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        if (delegate.contains(o)) {
            listener.beforeRemoveElement((T) o);
        }
        
        if (delegate.remove(o)) {
            listener.afterRemoveElement((T) o);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        
        for (T element : c) {
            changed |= add(element);
        }
        
        return changed;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        
        for (Object element : c) {
            changed |= remove(element);
        }
        
        return changed;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
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
    public void clear() {
        Iterator<T> iter = iterator();
        
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }
    
}
