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

import java.io.File;
import java.util.List;

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
     * Enforce static class.
     */
    private MiscUtil() {
    };
}
