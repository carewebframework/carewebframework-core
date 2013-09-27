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
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.SimpleDoubleSpinnerConstraint;

/**
 * Editor for double values.
 */
public class PropertyEditorDouble extends PropertyEditorBase {
    
    private final Doublebox doublebox;
    
    public PropertyEditorDouble() {
        super(new Doublebox());
        doublebox = (Doublebox) component;
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        doublebox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        Double min = propInfo.getConfigValueDouble("min", null);
        Double max = propInfo.getConfigValueDouble("max", null);
        
        if (min != null || max != null) {
            SimpleDoubleSpinnerConstraint constraint = new SimpleDoubleSpinnerConstraint();
            constraint.setMin(min);
            constraint.setMax(max);
            doublebox.setConstraint(constraint);
        }
    }
    
    @Override
    protected String getValue() {
        return doublebox.getText();
    }
    
    @Override
    protected void setValue(Object value) {
        doublebox.setText((String) value);
        updateValue();
    }
}
