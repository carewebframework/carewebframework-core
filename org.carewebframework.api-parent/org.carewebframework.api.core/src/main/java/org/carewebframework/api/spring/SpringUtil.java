/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.springframework.context.ApplicationContext;

/**
 * Static utility class for the access to Spring Framework services.
 */
public class SpringUtil {
    
    private static IAppContextFinder appContextFinder;
    
    private static PropertyProvider propertyProvider;
    
    /**
     * Sets the finder logic for locating the framework context. This is set during framework
     * initialization and should not be changed.
     * 
     * @param appContextFinder The application context finder.
     */
    public static void setAppContextFinder(IAppContextFinder appContextFinder) {
        SpringUtil.appContextFinder = appContextFinder;
        propertyProvider = new PropertyProvider(getRootAppContext());
    }
    
    /**
     * Returns the application context (container) associated with the current framework instance.
     * Will return null if an application context cannot be inferred or has not yet been created.
     * 
     * @return Application context
     */
    public static ApplicationContext getAppContext() {
        return appContextFinder == null ? null : appContextFinder.getAppContext();
    }
    
    /**
     * Returns the root application context (container) associated with the application. Will return
     * null if an application context cannot be inferred or has not yet been created.
     * 
     * @return Root application context
     */
    public static ApplicationContext getRootAppContext() {
        return appContextFinder == null ? null : appContextFinder.getRootAppContext();
    }
    
    /**
     * Returns true if an application context has been loaded.
     * 
     * @return boolean True if an application context has been loaded
     */
    public static boolean isLoaded() {
        return getAppContext() != null;
    }
    
    /**
     * Returns the bean with an id matching the specified id, or null if none found.
     * 
     * @param id Bean id
     * @return Returns the bean instance whose id matches the specified id, or null if none found or
     *         if the application context cannot be determined.
     */
    public static Object getBean(String id) {
        ApplicationContext appContext = getAppContext();
        return appContext == null ? null : appContext.containsBean(id) ? appContext.getBean(id) : null;
    }
    
    /**
     * Returns the bean with an id matching the specified id, or null if none found.
     * 
     * @param <T> Class of return type.
     * @param id Bean id
     * @param clazz Expected return type.
     * @return Returns the bean instance whose id matches the specified id, or null if none found or
     *         if the application context cannot be determined.
     */
    public static <T> T getBean(String id, Class<T> clazz) {
        ApplicationContext appContext = getAppContext();
        return appContext == null ? null
                : appContext.containsBean(id) && appContext.isTypeMatch(id, clazz) ? appContext.getBean(id, clazz) : null;
    }
    
    /**
     * Returns a property value from the application context.
     * 
     * @param name Property name.
     * @return Property value, or null if not found.
     */
    public static String getProperty(String name) {
        return propertyProvider.getProperty(name);
    }
    
    /**
     * Enforce static class.
     */
    private SpringUtil() {
    };
    
}
