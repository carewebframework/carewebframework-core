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

import java.awt.Color;

import org.fujion.common.ColorUtil;
import org.carewebframework.shell.property.PropertyInfo;
import org.fujion.component.ColorPicker;
import org.fujion.component.ColorPicker.ColorPickeritem;;

/**
 * Property editor for color properties. If the associated property has defined choices, the color
 * picker will be limited to those values only. Otherwise, the color palette is considered
 * unlimited.
 */
public class PropertyEditorColor extends PropertyEditorBase<ColorPicker> {

    public PropertyEditorColor() {
        super(new ColorPicker());
        editor.setShowText(true);
        //editor.setAutoAdd(true);
    }

    @Override
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        String[] values = propInfo.getConfigValueArray("values");

        if (values == null) {
            //component.setAutoAdd(true);
        } else {
            //component.setAutoAdd(false);
            editor.clear();

            for (String choice : values) {
                String[] color = choice.split("\\:", 2);

                for (int i = 0; i < color.length; i++) {
                    ColorPickeritem item = new ColorPickeritem(ColorUtil.toColor(color[i]));
                    editor.addChild(item);
                }
            }
        }
    }

    @Override
    protected String getValue() {
        Color value = editor.getValue();
        return value == null ? null : ColorUtil.toString(value);
    }

    @Override
    protected void setValue(Object value) {
        editor.setValue(ColorUtil.toColor((String) value));
        updateValue();
    }

}
