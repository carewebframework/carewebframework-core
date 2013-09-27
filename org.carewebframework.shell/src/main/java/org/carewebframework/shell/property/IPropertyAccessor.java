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

/**
 * Interface for read/write access to a property as defined by a PropInfo instance.
 */
public interface IPropertyAccessor {
    
    /**
     * Returns the value of the specified property.
     * 
     * @param propInfo The property descriptor.
     * @return The value of the property.
     * @throws Exception
     */
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception;
    
    /**
     * Sets the value of the specified property.
     * 
     * @param propInfo The property descriptor.
     * @param value The new value of the property.
     * @throws Exception
     */
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception;
    
}
