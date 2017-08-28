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

import org.carewebframework.shell.property.PropertyInfo;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Popupbox;
import org.fujion.component.Textbox;
import org.fujion.event.ChangeEvent;

/**
 * Editor for simple text.
 */
public class PropertyEditorText extends PropertyEditorBase<Popupbox> {

    @WiredComponent("popup.textbox")
    private Textbox textbox;

    public PropertyEditorText() {
        super(DesignConstants.RESOURCE_PREFIX + "propertyEditorText.fsp");
    }

    @Override
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        Integer maxLength = propInfo.getConfigValueInt("max", null);

        if (maxLength != null) {
            editor.setMaxLength(maxLength);
            textbox.setMaxLength(maxLength);
        }
    }

    @Override
    protected String getValue() {
        return editor.getValue();
    }

    @Override
    protected void setValue(Object value) {
        editor.setValue((String) value);
        textbox.setValue((String) value);
        updateValue();
    }

    @EventHandler(value = "open", target = "editor")
    private void onOpen() {
        textbox.focus();
    }

    @EventHandler(value = { "blur", "enter" }, target = "@textbox")
    private void onBlurOrEnter$textbox() {
        editor.close();
        editor.focus();
    }

    @EventHandler(value = "change", target = "@textbox")
    private void onChange$textbox(ChangeEvent event) {
        editor.setValue(event.getValue(String.class));
        super.onChange(event);
    }

    @Override
    protected void onChange(ChangeEvent event) {
        textbox.setValue(event.getValue(String.class));
        super.onChange(event);
    }
}
