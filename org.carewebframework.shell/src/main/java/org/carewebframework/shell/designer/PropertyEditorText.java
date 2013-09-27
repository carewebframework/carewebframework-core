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
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Textbox;

/**
 * Editor for simple text.
 */
public class PropertyEditorText extends PropertyEditorBase {
    
    private Bandbox bandbox;
    
    private Textbox textbox;
    
    public PropertyEditorText() throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorText.zul");
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        Integer maxLength = propInfo.getConfigValueInt("max", null);
        
        if (maxLength != null) {
            bandbox.setMaxlength(maxLength);
            textbox.setMaxlength(maxLength);
        }
        
        bandbox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        textbox.addForward(Events.ON_CHANGING, bandbox, Events.ON_CHANGE);
        bandbox.addForward(Events.ON_FOCUS, propGrid, Events.ON_SELECT);
        textbox.addForward(Events.ON_FOCUS, bandbox, Events.ON_SELECT);
    }
    
    @Override
    protected String getValue() {
        return bandbox.getText();
    }
    
    @Override
    protected void setValue(Object value) {
        bandbox.setText((String) value);
        textbox.setText((String) value);
        updateValue();
    }
    
    public void onChanging$textbox(InputEvent event) {
        bandbox.setRawValue(event.getValue());
    }
    
    public void onBlur$textbox() {
        bandbox.close();
        Events.echoEvent("onDelayedFocus", bandbox, bandbox);
    }
    
    public void onOK$textbox() {
        bandbox.close();
        Events.echoEvent("onDelayedFocus", bandbox, bandbox);
    }
    
    public void onChanging$bandbox(InputEvent event) {
        textbox.setRawValue(event.getValue());
    }
    
    public void onOpen$bandbox() {
        Events.echoEvent("onDelayedFocus", bandbox, bandbox.isOpen() ? textbox : bandbox);
    }
    
    public void onDelayedFocus$bandbox(Event event) {
        ((Textbox) ZKUtil.getEventOrigin(event).getData()).setFocus(true);
    }
}
