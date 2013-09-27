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

import java.util.Date;

import org.carewebframework.api.AbstractGlobalRegistry;
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
public class PropertyTypeRegistry extends AbstractGlobalRegistry<String, PropertyType> {
    
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
     * @param javaClass The java class of the return value.
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public static void register(String typeName, Class<?> javaClass, Class<? extends PropertyEditorBase> editorClass) {
        instance.add(typeName, javaClass, editorClass);
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
     * @param javaClass The java class of the return value.
     * @param editorClass The property editor class to use to edit this property's value.
     */
    public void add(String typeName, Class<?> javaClass, Class<? extends PropertyEditorBase> editorClass) {
        if (get(typeName) == null) {
            add(new PropertyType(typeName, javaClass, editorClass));
        }
    }
    
    /**
     * Add built-in property types.
     */
    private void init() {
        add("text", String.class, PropertyEditorText.class);
        add("color", String.class, PropertyEditorColor.class);
        add("choice", String.class, PropertyEditorChoiceList.class);
        add("enum", String.class, PropertyEditorEnum.class);
        add("action", String.class, PropertyEditorAction.class);
        add("icon", String.class, PropertyEditorIcon.class);
        add("integer", int.class, PropertyEditorInteger.class);
        add("double", double.class, PropertyEditorDouble.class);
        add("boolean", boolean.class, PropertyEditorBoolean.class);
        add("date", Date.class, PropertyEditorDate.class);
    }
    
    @Override
    protected String getKey(PropertyType item) {
        return item.getTypeName();
    }
    
}
