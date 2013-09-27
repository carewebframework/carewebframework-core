/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.zk.ColorPicker;

import org.zkoss.zk.ui.event.Events;

/**
 * Property editor for color properties. If the associated property has defined choices, the color
 * picker will be limited to those values only. Otherwise, the color palette is considered
 * unlimited.
 */
public class PropertyEditorColor extends PropertyEditorBase {
    
    private final ColorPicker colorPicker;
    
    public PropertyEditorColor() {
        super(new ColorPicker());
        colorPicker = (ColorPicker) component;
        colorPicker.setShowText(true);
        colorPicker.setAutoAdd(true);
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        component.addForward(ColorPicker.ON_SELECT_ITEM, propGrid, Events.ON_CHANGE);
        String[] values = propInfo.getConfigValueArray("values");
        
        if (values == null) {
            colorPicker.setAutoAdd(true);
        } else {
            colorPicker.setAutoAdd(false);
            colorPicker.clear();
            
            for (String choice : values) {
                String[] color = choice.split("\\:", 2);
                colorPicker.addColor(color[0], color.length == 2 ? color[1] : "");
            }
        }
    }
    
    @Override
    protected String getValue() {
        return colorPicker.getSelectedValue();
    }
    
    @Override
    protected void setValue(Object value) {
        colorPicker.setSelectedColor(value == null ? "" : value.toString());
        updateValue();
    }
}
