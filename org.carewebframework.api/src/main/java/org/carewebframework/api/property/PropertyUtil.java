/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.property;

import java.util.List;

import org.carewebframework.api.spring.SpringUtil;

/**
 * Static convenience class for accessing property services.
 */
public class PropertyUtil {
    
    private static IPropertyService propertyService;
    
    public static IPropertyService getPropertyService() {
        if (propertyService == null) {
            propertyService = SpringUtil.getBean("propertyService", IPropertyService.class);
        }
        
        return propertyService;
    }
    
    /**
     * Enforce static class.
     */
    private PropertyUtil() {
    }
    
    /**
     * @see IPropertyService#isAvailable
     */
    @SuppressWarnings("javadoc")
    public static boolean isAvailable() {
        return getPropertyService() != null && getPropertyService().isAvailable();
    }
    
    /**
     * @see IPropertyService#getValue
     */
    @SuppressWarnings("javadoc")
    public static String getValue(String propertyName) {
        return getPropertyService().getValue(propertyName, null);
    }
    
    /**
     * @see IPropertyService#getValue
     */
    @SuppressWarnings("javadoc")
    public static String getValue(String propertyName, String instanceName) {
        return getPropertyService().getValue(propertyName, instanceName);
    }
    
    /**
     * @see IPropertyService#getValues
     */
    @SuppressWarnings("javadoc")
    public static List<String> getValues(String propertyName) {
        return getPropertyService().getValues(propertyName, null);
    }
    
    /**
     * @see IPropertyService#getValues
     */
    @SuppressWarnings("javadoc")
    public static List<String> getValues(String propertyName, String instanceName) {
        return getPropertyService().getValues(propertyName, instanceName);
    }
    
    /**
     * @see IPropertyService#saveValue
     */
    @SuppressWarnings("javadoc")
    public static void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
        getPropertyService().saveValue(propertyName, instanceName, asGlobal, value);
    }
    
    /**
     * @see IPropertyService#getValues
     */
    @SuppressWarnings("javadoc")
    public static void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> value) {
        getPropertyService().saveValues(propertyName, instanceName, asGlobal, value);
    }
    
    /**
     * @see IPropertyService#getInstances
     */
    @SuppressWarnings("javadoc")
    public static List<String> getInstances(String propertyName, boolean asGlobal) {
        return getPropertyService().getInstances(propertyName, asGlobal);
    };
    
}
