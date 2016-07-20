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
    public static void setAttribute(String key, Object value) {
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
    public static Object getAttribute(String key) {
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
     * @param value Application name.
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
