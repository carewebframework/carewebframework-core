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
import org.fujion.component.Integerbox;

/**
 * Editor for integer values.
 */
public class PropertyEditorInteger extends PropertyEditorBase<Integerbox> {
    
    public PropertyEditorInteger() {
        super(new Integerbox());
    }
    
    @Override
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        editor.setMaxLength(9);
        editor.setMinValue(propInfo.getConfigValueInt("min", null));
        editor.setMaxValue(propInfo.getConfigValueInt("max", null));
    }
    
    @Override
    protected String getValue() {
        return Integer.toString(editor.getValue());
    }
    
    @Override
    protected void setValue(Object value) {
        editor.setValue(value == null ? null : (Integer) value);
        updateValue();
    }
    
}
