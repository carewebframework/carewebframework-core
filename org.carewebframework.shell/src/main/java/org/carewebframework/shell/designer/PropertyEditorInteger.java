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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.SimpleSpinnerConstraint;

/**
 * Editor for integer values.
 */
public class PropertyEditorInteger extends PropertyEditorBase {
    
    private final Intbox intbox;
    
    public PropertyEditorInteger() {
        super(new Intbox());
        intbox = (Intbox) component;
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        intbox.setMaxlength(9);
        intbox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        Integer min = propInfo.getConfigValueInt("min", null);
        Integer max = propInfo.getConfigValueInt("max", null);
        
        if (min != null || max != null) {
            SimpleSpinnerConstraint constraint = new SimpleSpinnerConstraint();
            constraint.setMin(min);
            constraint.setMax(max);
            intbox.setConstraint(constraint);
        }
    }
    
    @Override
    protected String getValue() {
        return intbox.getText();
    }
    
    @Override
    protected void setValue(Object value) {
        intbox.setText((String) value);
        updateValue();
    }
}
