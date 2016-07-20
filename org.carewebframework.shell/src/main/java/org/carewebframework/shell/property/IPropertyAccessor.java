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
     * @throws Exception Unspecified exception.
     */
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception;
    
    /**
     * Sets the value of the specified property.
     * 
     * @param propInfo The property descriptor.
     * @param value The new value of the property.
     * @throws Exception Unspecified exception.
     */
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception;
    
}
