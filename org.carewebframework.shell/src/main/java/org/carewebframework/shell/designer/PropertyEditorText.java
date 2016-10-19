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
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorText.cwf");
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
