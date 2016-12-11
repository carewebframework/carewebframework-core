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
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.InputEvent;
import org.carewebframework.web.event.SelectEvent;

/**
 * Editor for simple text.
 */
public class PropertyEditorText extends PropertyEditorBase<Popupbox> {
    
    @WiredComponent
    private Textbox textbox;
    
    public PropertyEditorText() throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "propertyEditorText.cwf");
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        Integer maxLength = propInfo.getConfigValueInt("max", null);
        
        if (maxLength != null) {
            component.setMaxLength(maxLength);
            textbox.setMaxLength(maxLength);
        }
        
        component.addEventForward(ChangeEvent.TYPE, propGrid.getWindow(), null);
        textbox.addEventForward(ChangeEvent.TYPE, component, null);
        component.addEventForward("focus", propGrid.getWindow(), SelectEvent.TYPE);
        textbox.addEventForward("focus", component, SelectEvent.TYPE);
    }
    
    @Override
    protected String getValue() {
        return component.getValue();
    }
    
    @Override
    protected void setValue(Object value) {
        component.setValue((String) value);
        textbox.setValue((String) value);
        updateValue();
    }
    
    @EventHandler(value = "change", target = "textbox")
    public void onChange$textbox(InputEvent event) {
        component.setValue(event.getValue());
    }
    
    @EventHandler(value = "blur", target = "textbox")
    public void onBlur$textbox() {
        component.close();
        EventUtil.post("onDelayedFocus", component, component);
    }
    
    @EventHandler(value = "enter", target = "textbox")
    public void onEnter$textbox() {
        component.close();
        EventUtil.post("onDelayedFocus", component, component);
    }
    
    @EventHandler(value = "change", target = "component")
    public void onChange$component(InputEvent event) {
        textbox.setValue(event.getValue());
    }
    
    public void onOpen$component() {
        EventUtil.post("onDelayedFocus", component, component.isOpen() ? textbox : component);
    }
    
    public void onDelayedFocus$component(Event event) {
        ((Textbox) event.getData()).setFocus(true);
    }
}
