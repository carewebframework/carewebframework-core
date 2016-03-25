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
     * /** Returns true if the service is available.
     * 
     * @return True if the service is available.
     * @see IPropertyService#isAvailable
     */
    public static boolean isAvailable() {
        return getPropertyService() != null && getPropertyService().isAvailable();
    }
    
    /**
     * Returns a property value as a string.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @return The property value, or null if not found.
     * @see IPropertyService#getValue
     */
    public static String getValue(String propertyName) {
        return getPropertyService().getValue(propertyName, null);
    }
    
    /**
     * Returns a property value as a string.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @param instanceName An optional instance name. Use null to indicate the default instance.
     * @return The property value, or null if not found.
     * @see IPropertyService#getValue
     */
    public static String getValue(String propertyName, String instanceName) {
        return getPropertyService().getValue(propertyName, instanceName);
    }
    
    /**
     * Returns a property value as a string list.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @return The property value as a string list, or null if not found.
     * @see IPropertyService#getValues
     */
    public static List<String> getValues(String propertyName) {
        return getPropertyService().getValues(propertyName, null);
    }
    
    /**
     * Returns a property value as a string list.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @return The property value as a string list, or null if not found.
     * @see IPropertyService#getValues
     */
    public static List<String> getValues(String propertyName, String instanceName) {
        return getPropertyService().getValues(propertyName, instanceName);
    }
    
    /**
     * Saves a string value to the underlying property store.
     * 
     * @param propertyName Name of the property to be saved.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @param asGlobal If true, save as a global property. If false, save as a user property.
     * @param value Value to be saved. If null, any existing value is removed.
     * @see IPropertyService#saveValue
     */
    public static void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
        getPropertyService().saveValue(propertyName, instanceName, asGlobal, value);
    }
    
    /**
     * Saves a value list to the underlying property store.
     * 
     * @param propertyName Name of the property to be saved.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @param asGlobal If true, save as a global property. If false, save as a user property.
     * @param value Value to be saved. If null or empty, any existing values are removed.
     * @see IPropertyService#getValues
     */
    public static void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> value) {
        getPropertyService().saveValues(propertyName, instanceName, asGlobal, value);
    }
    
    /**
     * Returns a list of all instance id's associated with the specified property name.
     * 
     * @param propertyName Name of the property.
     * @param asGlobal Accesses the global property store if true, the user property store if false.
     * @return A list of associated instance id's. May be empty, but never null.
     * @see IPropertyService#getInstances
     */
    public static List<String> getInstances(String propertyName, boolean asGlobal) {
        return getPropertyService().getInstances(propertyName, asGlobal);
    };
    
}
