/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import java.util.Map;

import org.carewebframework.api.spring.SpringUtil;

import org.springframework.util.Assert;

/**
 * Static utility class for the framework operations.
 */
public class FrameworkUtil {
    
    /**
     * Returns true if the application framework has been initialized.
     * 
     * @return boolean whether AppFramework has been initialized
     */
    public static boolean isInitialized() {
        return getAppFramework() != null;
    }
    
    /**
     * Returns the application framework associated with the current framework instance.
     * 
     * @return Application framework associated with the current framework instance.
     */
    public static AppFramework getAppFramework() {
        return SpringUtil.getBean("appFramework", AppFramework.class);
    }
    
    /**
     * Returns a reference to the attribute map used to store persistent references to arbitrary
     * objects.
     * 
     * @return Attribute map
     */
    public static Map<String, Object> getAttributes() {
        return isInitialized() ? getAppFramework().getAttributes() : null;
    }
    
    /**
     * Stores an arbitrary named attribute in the attribute cache.
     * 
     * @param key Attribute name.
     * @param value Attribute value. If null, value is removed from cache.
     * @throws IllegalStateException if AppFramework is not initialized
     */
    public static void setAttribute(final String key, final Object value) {
        assertInitialized();
        getAppFramework().setAttribute(key, value);
    }
    
    /**
     * Retrieves the value of a named attribute from the attribute cache.
     * 
     * @param key Attribute name
     * @return Attribute value.
     * @throws IllegalStateException if AppFramework is not initialized
     */
    public static Object getAttribute(final String key) {
        assertInitialized();
        return getAppFramework().getAttribute(key);
    }
    
    /**
     * Return the application name from the context.
     * 
     * @return The application name.
     */
    public static String getAppName() {
        return (String) getAttribute("applicationName");
    }
    
    /**
     * Set the application name into the context.
     * 
     * @param value
     */
    public static void setAppName(String value) {
        setAttribute("applicationName", value);
    }
    
    /**
     * Asserts that the application framework has been initialized.
     */
    public static void assertInitialized() {
        Assert.state(isInitialized(), "AppFramework must be initialized");
    }
    
    /**
     * Enforce static class.
     */
    private FrameworkUtil() {
    };
    
}
