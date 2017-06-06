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
package org.carewebframework.api.spring;

import java.io.IOException;

import org.carewebframework.common.MiscUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Static utility class for the access to Spring Framework services.
 */
public class SpringUtil {
    
    private static IAppContextFinder appContextFinder;
    
    private static volatile PropertyProvider propertyProvider;
    
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    
    /**
     * Sets the finder logic for locating the framework context. This is set during framework
     * initialization and should not be changed.
     *
     * @param appContextFinder The application context finder.
     */
    public static void setAppContextFinder(IAppContextFinder appContextFinder) {
        SpringUtil.appContextFinder = appContextFinder;
    }
    
    /**
     * Returns the application context (container) associated with the current framework instance.
     * Will return null if an application context cannot be inferred or has not yet been created.
     *
     * @return Application context
     */
    public static ApplicationContext getAppContext() {
        return appContextFinder == null ? null : appContextFinder.getChildAppContext();
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
     * Return the bean of the specified class.
     *
     * @param clazz The bean class.
     * @return The requested bean instance, or null if not found.
     */
    public static <T> T getBean(Class<T> clazz) {
        ApplicationContext appContext = getAppContext();
        
        try {
            return appContext == null ? null : appContext.getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns a property value from the application context.
     *
     * @param name Property name.
     * @return Property value, or null if not found.
     */
    public static String getProperty(String name) {
        if (propertyProvider == null) {
            initPropertyProvider();
        }
        
        return propertyProvider.getProperty(name);
    }
    
    private static synchronized void initPropertyProvider() {
        if (propertyProvider == null) {
            propertyProvider = new PropertyProvider(getRootAppContext());
        }
    }
    
    /**
     * Returns the resource at the specified location. Supports classpath references.
     *
     * @param location The resource location.
     * @return The corresponding resource, or null if one does not exist.
     */
    public static Resource getResource(String location) {
        Resource resource = resolver.getResource(location);
        return resource.exists() ? resource : null;
    }
    
    /**
     * Returns an array of resources matching the location pattern.
     *
     * @param locationPattern The location pattern. Supports classpath references.
     * @return Array of matching resources.
     */
    public static Resource[] getResources(String locationPattern) {
        try {
            return resolver.getResources(locationPattern);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Enforce static class.
     */
    private SpringUtil() {
    }
    
}
