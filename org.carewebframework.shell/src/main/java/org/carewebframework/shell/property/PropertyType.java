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

import org.carewebframework.shell.designer.PropertyEditorBase;

/**
 * Represents information about a specific property data type.
 */
public class PropertyType {
    
    private final String typeName;
    
    private final Class<? extends PropertyEditorBase<?>> editorClass;
    
    private final PropertySerializer<Object> serializer;
    
    /**
     * Creates a property type with the specified attributes.
     * 
     * @param typeName The name of the property type as used in the plug-in declaration.
     * @param serializer Serializer for properties of this type.
     * @param editorClass The property editor class to use to edit this property's value.
     */
    @SuppressWarnings("unchecked")
    public PropertyType(String typeName, PropertySerializer<?> serializer,
        Class<? extends PropertyEditorBase<?>> editorClass) {
        this.typeName = typeName;
        this.serializer = (PropertySerializer<Object>) serializer;
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
     * Returns the class of the associated property editor.
     * 
     * @return Class of the associated property editor.
     */
    public Class<? extends PropertyEditorBase<?>> getEditorClass() {
        return editorClass;
    }
    
    /**
     * Returns the property serializer for this property type.
     * 
     * @return Property serializer.
     */
    public PropertySerializer<Object> getSerializer() {
        return serializer;
    }
}
