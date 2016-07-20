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
package org.carewebframework.api.property;

import java.util.List;

/**
 * Interface for setting and retrieving values from an underlying data store.
 */
public interface IPropertyService {
    
    static final String ALIAS_TYPE_PROPERTY = "PROPERTY";
    
    /**
     * Returns true if the service is available.
     * 
     * @return True if the service is available.
     */
    boolean isAvailable();
    
    /**
     * Returns a property value as a string.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @param instanceName An optional instance name. Use null to indicate the default instance.
     * @return The property value, or null if not found.
     */
    String getValue(String propertyName, String instanceName);
    
    /**
     * Returns a property value as a string list.
     * 
     * @param propertyName Name of the property whose value is sought.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @return The property value as a string list, or null if not found.
     */
    List<String> getValues(String propertyName, String instanceName);
    
    /**
     * Saves a string value to the underlying property store.
     * 
     * @param propertyName Name of the property to be saved.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @param asGlobal If true, save as a global property. If false, save as a user property.
     * @param value Value to be saved. If null, any existing value is removed.
     */
    void saveValue(String propertyName, String instanceName, boolean asGlobal, String value);
    
    /**
     * Saves a value list to the underlying property store.
     * 
     * @param propertyName Name of the property to be saved.
     * @param instanceName An optional instance name. Specify null to indicate the default instance.
     * @param asGlobal If true, save as a global property. If false, save as a user property.
     * @param value Value to be saved. If null or empty, any existing values are removed.
     */
    void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> value);
    
    /**
     * Returns a list of all instance id's associated with the specified property name.
     * 
     * @param propertyName Name of the property.
     * @param asGlobal Accesses the global property store if true, the user property store if false.
     * @return A list of associated instance id's. May be empty, but never null.
     */
    List<String> getInstances(String propertyName, boolean asGlobal);
    
}
