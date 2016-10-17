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
 * Wraps a collection and notifies a listener of all changes.
 *
 * @param <T> The element type.
 */
public class ObservedCollection<T> extends ProxiedCollection<T> {
    
    public interface IObservedCollectionListener<T> {
        
        void onAddElement(T element);
        
        void onRemoveElement(T element);
    }
    
    public ObservedCollection(Collection<T> delegate, IObservedCollectionListener<T> listener) {
        super(delegate, new ProxiedCollection.IProxiedCollectionOperations<T>() {
            
            @Override
            public boolean add(T element, Collection<T> delegate) {
                if (delegate.add(element)) {
                    listener.onAddElement(element);
                    return true;
                }
                
                return false;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public boolean remove(Object element, Collection<T> delegate) {
                if (delegate.remove(element)) {
                    listener.onRemoveElement((T) element);
                    return true;
                }
                
                return false;
            }
            
            @Override
            public void remove(T element, Iterator<T> iterator) {
                iterator.remove();
                listener.onRemoveElement(element);
            }
            
        });
    }
    
}
