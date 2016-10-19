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
package org.carewebframework.ui.wonderbar;

import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseUIComponent;

/**
 * Base class for wonder bar item and group.
 */
public abstract class WonderbarAbstractItem extends BaseUIComponent {
    
    private String value;
    
    private String label;
    
    protected WonderbarAbstractItem() {
    }
    
    protected WonderbarAbstractItem(String label) {
        this(label, null);
    }
    
    protected WonderbarAbstractItem(String label, String value) {
        this(label, value, null);
    }
    
    protected WonderbarAbstractItem(String label, String value, Object data) {
        this();
        setLabel(label);
        setValue(value);
        setData(data);
    }
    
    @Override
    public String toString() {
        return value == null ? label : value;
    }
    
    /**
     * Returns the value property. When present, the contents of value, rather than that of the
     * label, is placed in the input box when the associated item is selected.
     * 
     * @return The value property
     */
    @PropertyGetter("value")
    protected String getValue() {
        return value;
    }
    
    /**
     * Sets the value property. When present, the contents of value, rather than that of the label,
     * is placed in the input box when the associated item is selected.
     * 
     * @param value The value property
     */
    @PropertySetter("value")
    protected void setValue(String value) {
        if (!areEqual(value, this.value)) {
            sync("value", this.value = value);
        }
    }
    
    /**
     * Returns the label text. The label text determines what is displayed in the choice list.
     * Unless the value property is set, the label text is place in the input box when the
     * associated item is selected.
     * 
     * @return The label text.
     */
    @PropertyGetter("label")
    protected String getLabel() {
        return label;
    }
    
    /**
     * Sets the label text. The label text determines what is displayed in the choice list. Unless
     * the value property is set, the label text is place in the input box when the associated item
     * is selected.
     * 
     * @param label The label text.
     */
    @PropertySetter("label")
    protected void setLabel(String label) {
        if (!areEqual(label, this.label)) {
            sync("label", this.label = label);
        }
    }
    
}
