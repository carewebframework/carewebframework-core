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
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

/**
 * Editor for boolean values.
 */
public class PropertyEditorBoolean extends PropertyEditorBase {
    
    private Radiogroup radiogroup;
    
    /**
     * Create property editor.
     * 
     * @throws Exception Unspecified exception.
     */
    public PropertyEditorBoolean() throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorBoolean.zul");
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        radiogroup.addForward(Events.ON_CHECK, propGrid, Events.ON_CHANGE);
        radiogroup.addForward(Events.ON_CLICK, propGrid, Events.ON_SELECT);
        component.addForward(Events.ON_CLICK, propGrid, Events.ON_SELECT);
        
        for (Radio radio : radiogroup.getItems()) {
            String label = propInfo.getConfigValue(radio.getLabel().trim());
            
            if (label != null) {
                radio.setLabel(label);
            }
        }
    }
    
    /**
     * Sets focus to the selected radio button.
     */
    @Override
    public void setFocus() {
        Radio radio = radiogroup.getSelectedItem();
        
        if (radio == null) {
            radio = radiogroup.getItems().get(0);
        }
        
        radio.setFocus(true);
    }
    
    @Override
    protected Boolean getValue() {
        int i = radiogroup.getSelectedIndex();
        return i < 0 ? null : i == 0;
    }
    
    @Override
    protected void setValue(Object value) {
        int i = value == null ? -1 : (Boolean) value ? 0 : 1;
        radiogroup.setSelectedIndex(i);
    }
}
