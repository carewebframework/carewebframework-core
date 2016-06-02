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
