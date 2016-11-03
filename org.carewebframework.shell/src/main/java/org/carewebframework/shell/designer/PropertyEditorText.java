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
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.InputEvent;

/**
 * Editor for simple text.
 */
public class PropertyEditorText extends PropertyEditorBase {
    
    private Popupbox popupbox;
    
    private Textbox textbox;
    
    public PropertyEditorText() throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorText.cwf");
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        Integer maxLength = propInfo.getConfigValueInt("max", null);
        
        if (maxLength != null) {
            popupbox.setMaxLength(maxLength);
            textbox.setMaxLength(maxLength);
        }
        
        popupbox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        textbox.addForward(Events.ON_CHANGING, popupbox, Events.ON_CHANGE);
        popupbox.addForward(Events.ON_FOCUS, propGrid, Events.ON_SELECT);
        textbox.addForward(Events.ON_FOCUS, popupbox, Events.ON_SELECT);
    }
    
    @Override
    protected String getValue() {
        return popupbox.getValue();
    }
    
    @Override
    protected void setValue(Object value) {
        popupbox.setValue((String) value);
        textbox.setValue((String) value);
        updateValue();
    }
    
    public void onChanging$textbox(InputEvent event) {
        popupbox.setValue(event.getValue());
    }
    
    public void onBlur$textbox() {
        popupbox.close();
        EventUtil.post("onDelayedFocus", popupbox, popupbox);
    }
    
    public void onOK$textbox() {
        popupbox.close();
        EventUtil.post("onDelayedFocus", popupbox, popupbox);
    }
    
    public void onChanging$popupbox(InputEvent event) {
        textbox.setValue(event.getValue());
    }
    
    public void onOpen$popupbox() {
        Events.echoEvent("onDelayedFocus", popupbox, popupbox.isOpen() ? textbox : popupbox);
    }
    
    public void onDelayedFocus$popupbox(Event event) {
        ((Textbox) event.getData()).setFocus(true);
    }
}
