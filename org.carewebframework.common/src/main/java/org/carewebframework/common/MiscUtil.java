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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.UnhandledException;

/**
 * Miscellaneous utility methods.
 */
public class MiscUtil {
    
    /**
     * Returns true if the specified file exists.
     * 
     * @param fileName File name.
     * @return True if file exists.
     */
    public static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }
    
    /**
     * Returns true if the list contains the exact instance of the specified object.
     * 
     * @param list List to search.
     * @param object Object instance to locate.
     * @return True if the object was found.
     */
    public static boolean containsInstance(List<?> list, Object object) {
        return indexOfInstance(list, object) > -1;
    }
    
    /**
     * Performs a lookup for the exact instance of an object in the list and returns its index, or
     * -1 if not found. This is different from the usual implementation of a list search that uses
     * the object's equals implementation.
     * 
     * @param list List to search.
     * @param object Object instance to locate.
     * @return Index of the object.
     */
    public static int indexOfInstance(List<?> list, Object object) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == object) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Casts a list containing elements of class T to a list containing elements of a subclass E.
     * 
     * @param list List to be recast.
     * @param clazz Class to which to cast list elements.
     * @return The recast list.
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends T> List<E> castList(List<T> list, Class<E> clazz) {
        return (List<E>) list;
    }
    
    /**
     * Returns a list iterator that produces only collection elements of the specified type.
     * 
     * @param collection Collection to iterate.
     * @param type Type of element to return.
     * @return An iterator.
     */
    public static <T, S extends T> Iterator<S> iteratorForType(Collection<T> collection, Class<S> type) {
        
        return new Iterator<S>() {
            
            Iterator<T> iter = collection.iterator();
            
            S next;
            
            boolean needsNext = true;
            
            @Override
            public boolean hasNext() {
                return nxt() != null;
            }
            
            @Override
            public S next() {
                S result = nxt();
                needsNext = true;
                return result;
            }
            
            @Override
            public void remove() {
                iter.remove();
            }
            
            @SuppressWarnings("unchecked")
            private S nxt() {
                if (needsNext) {
                    next = null;
                    needsNext = false;
                    
                    while (iter.hasNext()) {
                        T nxt = iter.next();
                        
                        if (type.isInstance(nxt)) {
                            next = (S) nxt;
                            break;
                        }
                    }
                }
                
                return next;
            }
            
        };
    }
    
    /**
     * Returns an iterable that produces only collection members of the specified type.
     * 
     * @param collection Collection to iterate.
     * @param type Type of element to return.
     * @return An iterable.
     */
    public static <T, S extends T> Iterable<S> iterableForType(Collection<T> collection, Class<S> type) {
        return new Iterable<S>() {
            
            @Override
            public Iterator<S> iterator() {
                return iteratorForType(collection, type);
            }
            
        };
    }
    
    /**
     * Converts a checked exception to unchecked. If the original exception is already unchecked, it
     * is simply returned.
     * 
     * @param e The original exception.
     * @return The returned unchecked exception.
     */
    public static RuntimeException toUnchecked(Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        
        return new UnhandledException(e);
    }
    
    /**
     * Enforce static class.
     */
    private MiscUtil() {
    }
}
