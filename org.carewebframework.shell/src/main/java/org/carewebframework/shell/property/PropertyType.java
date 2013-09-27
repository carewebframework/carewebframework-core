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

import org.carewebframework.shell.designer.PropertyEditorBase;

/**
 * Represents information about a specific property data type.
 */
public class PropertyType {
    
    private final String typeName;
    
    private final Class<?> javaClass;
    
    private final Class<? extends PropertyEditorBase> editorClass;
    
    /**
     * Creates a property type with the specified attributes.
     * 
     * @param typeName The name of the property type as used in the plug-in declaration.
     * @param javaClass The java class of the return value.
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public PropertyType(String typeName, Class<?> javaClass, Class<? extends PropertyEditorBase> editorClass) {
        this.typeName = typeName;
        this.javaClass = javaClass;
        this.editorClass = editorClass;
    }
    
    /**
     * Returns the name of this property type.
     * 
     * @return The name of the property type.
     */
    public String getTypeName() {
        return typeName;
    }
    
    /**
     * Returns the class of the return type.
     * 
     * @return Class of the return type.
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }
    
    /**
     * Returns the class of the associated property editor.
     * 
     * @return Class of the associated property editor.
     */
    public Class<? extends PropertyEditorBase> getEditorClass() {
        return editorClass;
    }
    
}
