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
package org.carewebframework.shell.designer;

import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.util.CWFUtil;
import org.fujion.annotation.EventHandler;
import org.fujion.component.BaseUIComponent;
import org.fujion.event.ChangeEvent;
import org.fujion.page.PageUtil;

/**
 * All property editors must descend from this abstract class.
 *
 * @param <T> The input component class.
 */
public abstract class PropertyEditorBase<T extends BaseUIComponent> implements Comparable<PropertyEditorBase<T>> {

    protected final T editor;

    private Object value;

    private Object target;

    private PropertyGrid propGrid;

    private PropertyInfo propInfo;

    private int index;

    /**
     * Create property editor using the specified template.
     *
     * @param template The template to create the editing component.
     */
    @SuppressWarnings("unchecked")
    protected PropertyEditorBase(String template) {
        this((T) PageUtil.createPage(template, null).get(0));
    }

    /**
     * Create property editor using the specified component for editing.
     *
     * @param editor The component used to edit the property.
     */
    protected PropertyEditorBase(T editor) {
        this.editor = editor;
        editor.setName("editor");
        editor.setWidth("95%");
    }

    protected void wireController() {
        editor.wireController(this);
    }

    /**
     * Returns the component used to edit the property.
     *
     * @return The editor component.
     */
    public T getEditor() {
        return editor;
    }

    /**
     * Logic to return the value from the editor component.
     *
     * @return Value from the editor component.
     */
    protected abstract Object getValue();

    /**
     * Logic to set the value in the editor component.
     *
     * @param value Value for the editor component.
     */
    protected abstract void setValue(Object value);

    /**
     * Returns true if the property value has been changed since the last commit.
     *
     * @return True if pending changes exist.
     */
    public boolean hasChanged() {
        Object currentValue = getValue();
        return value == null || currentValue == null ? value != currentValue : !value.equals(currentValue);
    }

    /**
     * Returns the PropertyInfo object associated with this editor.
     *
     * @return PropInfo object.
     */
    public PropertyInfo getPropInfo() {
        return propInfo;
    }

    /**
     * Returns the target object associated with this editor.
     *
     * @return Target object.
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Updates the last committed value.
     */
    public void updateValue() {
        value = getValue();
    }

    /**
     * Sets focus to the editor component.
     */
    public void setFocus() {
        editor.setFocus(true);
    }

    /**
     * Initializes the property editor.
     *
     * @param target The target object.
     * @param propInfo The PropertyInfo instance reflecting the property being edited on the target.
     * @param propGrid The property grid owning this property editor.
     */
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        this.target = target;
        this.propInfo = propInfo;
        this.propGrid = propGrid;
        this.index = propGrid.getEditorCount();
        wireController();
    }

    /**
     * Commit the changed value.
     *
     * @return True if the operation was successful.
     */
    public boolean commit() {
        try {
            setWrongValueMessage(null);
            propInfo.setPropertyValue(target, getValue());
            updateValue();
            return true;
        } catch (Exception e) {
            setWrongValueException(e);
            return false;
        }
    }

    /**
     * Revert changes to the property value.
     *
     * @return True if the operation was successful.
     */
    public boolean revert() {
        try {
            setWrongValueMessage(null);
            setValue(propInfo.getPropertyValue(target));
            updateValue();
            return true;
        } catch (Exception e) {
            setWrongValueException(e);
            return false;
        }
    }

    /**
     * Updates the wrong value message at the client.
     *
     * @param exc The exception to display.
     */
    public void setWrongValueException(Throwable exc) {
        setWrongValueMessage(CWFUtil.formatExceptionForDisplay(exc));
    }

    /**
     * Updates the wrong value message at the client.
     *
     * @param message If null, any existing message is removed. Otherwise, the specified method is
     *            displayed next to the editor component.
     */
    public void setWrongValueMessage(String message) {
        editor.setBalloon(message);
    }

    @Override
    public int compareTo(PropertyEditorBase<T> o) {
        return index - o.index;
    }

    /**
     * Notify the property grid that the value has changed.
     *
     * @param event The change event.
     */
    @EventHandler(value = "change", target = "@editor")
    protected void onChange(ChangeEvent event) {
        if (hasChanged()) {
            propGrid.propertyChanged();
        }
    }

    @EventHandler(value = "focus", target = "@editor")
    private void onFocus() {
        propGrid.findEditor(propInfo.getName(), true);
    }
}
