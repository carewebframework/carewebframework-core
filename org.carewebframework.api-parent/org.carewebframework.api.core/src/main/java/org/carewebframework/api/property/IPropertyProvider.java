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

/**
 * Generic interface for reading property values from an arbitrary source.
 */
public interface IPropertyProvider {
    
    /**
     * Returns a property value as a string.
     * 
     * @param key Name of the property whose value is sought.
     * @return The property's value, or null if not found.
     */
    String getProperty(String key);
    
    /**
     * Returns true if the property exists.
     * 
     * @param key The property name.
     * @return True if the property exists.
     */
    boolean hasProperty(String key);
}
