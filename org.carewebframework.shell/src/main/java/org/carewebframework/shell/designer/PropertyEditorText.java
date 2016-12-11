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
public class PropertyEditorText extends PropertyEditorBase<Bandbox> {
    
    private Textbox textbox;
    
    public PropertyEditorText() throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorText.zul");
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        Integer maxLength = propInfo.getConfigValueInt("max", null);
        
        if (maxLength != null) {
            editor.setMaxlength(maxLength);
            textbox.setMaxlength(maxLength);
        }
        
        editor.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        textbox.addForward(Events.ON_CHANGING, editor, Events.ON_CHANGE);
        editor.addForward(Events.ON_FOCUS, propGrid, Events.ON_SELECT);
        textbox.addForward(Events.ON_FOCUS, editor, Events.ON_SELECT);
    }
    
    @Override
    protected String getValue() {
        return editor.getText();
    }
    
    @Override
    protected void setValue(Object value) {
        editor.setText((String) value);
        textbox.setText((String) value);
        updateValue();
    }
    
    public void onChanging$textbox(InputEvent event) {
        editor.setRawValue(event.getValue());
    }
    
    public void onBlur$textbox() {
        editor.close();
        Events.echoEvent("onDelayedFocus", editor, editor);
    }
    
    public void onOK$textbox() {
        editor.close();
        Events.echoEvent("onDelayedFocus", editor, editor);
    }
    
    public void onChanging$editor(InputEvent event) {
        textbox.setRawValue(event.getValue());
    }
    
    public void onOpen$editor() {
        Events.echoEvent("onDelayedFocus", editor, editor.isOpen() ? textbox : editor);
    }
    
    public void onDelayedFocus$editor(Event event) {
        ((Textbox) ZKUtil.getEventOrigin(event).getData()).setFocus(true);
    }
}
