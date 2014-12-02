/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.property;

import java.lang.reflect.Method;

import org.springframework.util.TypeUtils;

/**
 * Utility methods for manipulating properties.
 */
public class PropertyUtil {
    
    /**
     * Returns the requested setter method from an object instance.
     * 
     * @param methodName Name of the setter method.
     * @param instance Object instance to search.
     * @param valueClass The setter parameter type (null if don't care).
     * @return The setter method.
     * @throws NoSuchMethodException If method was not found.
     */
    public static Method findSetter(String methodName, Object instance, Class<?> valueClass) throws NoSuchMethodException {
        return findMethod(methodName, instance, valueClass, true);
    }
    
    /**
     * Returns the requested getter method from an object instance.
     * 
     * @param methodName Name of the getter method.
     * @param instance Object instance to search.
     * @param valueClass The return value type (null if don't care).
     * @return The getter method.
     * @throws NoSuchMethodException If method was not found.
     */
    public static Method findGetter(String methodName, Object instance, Class<?> valueClass) throws NoSuchMethodException {
        return findMethod(methodName, instance, valueClass, false);
    }
    
    /**
     * Returns the requested method from an object instance.
     * 
     * @param methodName Name of the setter method.
     * @param instance Object instance to search.
     * @param valueClass The desired property return type (null if don't care).
     * @param setter If true, search for setter method signature. If false, getter method signature.
     * @return The requested method.
     * @throws NoSuchMethodException If method was not found.
     */
    private static Method findMethod(String methodName, Object instance, Class<?> valueClass, boolean setter)
                                                                                                             throws NoSuchMethodException {
        if (methodName == null) {
            return null;
        }
        
        int paramCount = setter ? 1 : 0;
        
        for (Method method : instance.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == paramCount) {
                Class<?> targetClass = setter ? method.getParameterTypes()[0] : method.getReturnType();
                
                if (valueClass == null || TypeUtils.isAssignable(targetClass, valueClass)) {
                    return method;
                }
            }
        }
        
        throw new NoSuchMethodException("Compatible method not found: " + methodName);
        
    }
    
    /**
     * Enforce static class.
     */
    private PropertyUtil() {
    }
}
