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

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.shell.designer.PropertyEditorAction;
import org.carewebframework.shell.designer.PropertyEditorBase;
import org.carewebframework.shell.designer.PropertyEditorBoolean;
import org.carewebframework.shell.designer.PropertyEditorChoiceList;
import org.carewebframework.shell.designer.PropertyEditorColor;
import org.carewebframework.shell.designer.PropertyEditorDate;
import org.carewebframework.shell.designer.PropertyEditorDouble;
import org.carewebframework.shell.designer.PropertyEditorEnum;
import org.carewebframework.shell.designer.PropertyEditorIcon;
import org.carewebframework.shell.designer.PropertyEditorInteger;
import org.carewebframework.shell.designer.PropertyEditorText;

/**
 * Registry of all supported property types.
 */
public class PropertyTypeRegistry extends AbstractRegistry<String, PropertyType> {
    
    private static final PropertyTypeRegistry instance = new PropertyTypeRegistry();
    
    /**
     * Returns the singleton instance of the property type registry.
     * 
     * @return Reference to the property type registry.
     */
    public static PropertyTypeRegistry getInstance() {
        return instance;
    }
    
    /**
     * Registers a property type.
     * 
     * @param typeName The name of the property type as used in the plug-in declaration.
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public static void register(String typeName, Class<? extends PropertyEditorBase<?>> editorClass) {
        register(typeName, null, editorClass);
    }
    
    /**
     * Registers a property type.
     * 
     * @param typeName The name of the property type as used in the plug-in declaration.
     * @param serializer Serializer for this property type (may be null).
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public static void register(String typeName, PropertySerializer<?> serializer,
                                Class<? extends PropertyEditorBase<?>> editorClass) {
        instance.add(typeName, serializer, editorClass);
    }
    
    /**
     * Registers several built-in property types.
     */
    public PropertyTypeRegistry() {
        super();
        init();
    }
    
    /**
     * Registers a property type.
     * 
     * @param typeName The name of the property type as used in the plug-in declaration.
     * @param serializer Serializer for this property type (may be null).
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public void add(String typeName, PropertySerializer<?> serializer, Class<? extends PropertyEditorBase<?>> editorClass) {
        if (get(typeName) == null) {
            register(new PropertyType(typeName, serializer, editorClass));
        }
    }
    
    /**
     * Add built-in property types.
     */
    private void init() {
        add("text", PropertySerializer.STRING, PropertyEditorText.class);
        add("color", PropertySerializer.STRING, PropertyEditorColor.class);
        add("choice", PropertySerializer.STRING, PropertyEditorChoiceList.class);
        add("action", PropertySerializer.STRING, PropertyEditorAction.class);
        add("icon", PropertySerializer.STRING, PropertyEditorIcon.class);
        add("integer", PropertySerializer.INTEGER, PropertyEditorInteger.class);
        add("double", PropertySerializer.DOUBLE, PropertyEditorDouble.class);
        add("boolean", PropertySerializer.BOOLEAN, PropertyEditorBoolean.class);
        add("date", PropertySerializer.DATE, PropertyEditorDate.class);
    }
    
    @Override
    protected String getKey(PropertyType item) {
        return item.getTypeName();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public PropertyType get(String key) {
        PropertyType propType = super.get(key);
        
        if (propType != null || !key.startsWith("enum:")) {
            return propType;
        }
        
        String[] pcs = key.split("\\:", 3);
        PropertySerializer<?> serializer = null;
        
        if ("bean".equals(pcs[1])) {
            serializer = new PropertySerializer.IterableSerializer(pcs[2]);
        } else if ("class".equals(pcs[1])) {
            try {
                Class<?> clazz = Class.forName(pcs[2]);
                serializer = clazz.isEnum() ? new PropertySerializer.EnumSerializer((Class<Enum>) clazz)
                        : Iterable.class.isAssignableFrom(clazz)
                                ? new PropertySerializer.IterableSerializer((Class<Iterable>) clazz) : null;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        if (serializer == null) {
            throw new IllegalArgumentException("Not an enumerable type: " + key);
        }
        
        propType = new PropertyType(key, serializer, PropertyEditorEnum.class);
        register(propType);
        return propType;
    }
    
}
