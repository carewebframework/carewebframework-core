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

import org.carewebframework.shell.elements.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.KeyCode;
import org.carewebframework.web.event.KeyEvent;
import org.carewebframework.web.event.KeycaptureEvent;
import org.springframework.util.StringUtils;

/**
 * Base class for component-based property editors.
 */
public class PropertyEditorList extends PropertyEditorBase<Combobox> {
    
    private String delimiter;
    
    /**
     * Pressing delete key will clear combo box selection when set to read-only.
     */
    private final IEventListener deleteListener = (event) -> {
        KeyEvent evt = (KeyEvent) event;
        
        if (evt.getKeyCode() == KeyCode.VK_DELETE) {
            boolean changed = !StringUtils.isEmpty(editor.getValue());
            editor.setValue(null);
            //TODO: editor.close();
            
            if (changed) {
                EventUtil.post(ChangeEvent.TYPE, editor, null);
            }
        }
    };
    
    /**
     * Create property editor.
     */
    public PropertyEditorList() {
        super(new Combobox());
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, final PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        setReadonly(propInfo.getConfigValueBoolean("readonly", true));
        delimiter = propInfo.getConfigValue("delimiter");
    }
    
    /**
     * Set read only mode.
     * 
     * @param readonly If true, the component is read only and the delete key (or ctrl-x) will
     *            remove the selection. If false, the combo box content can be modified and the
     *            delete key functions normally.
     */
    private void setReadonly(boolean readonly) {
        editor.setReadonly(readonly);
        editor.setKeycapture(readonly ? "DELETE" : null);
        
        if (readonly) {
            editor.addEventListener(KeycaptureEvent.TYPE, deleteListener);
        } else {
            editor.removeEventListener(KeycaptureEvent.TYPE, deleteListener);
        }
    }
    
    /**
     * Append a new combo item using the specified label. If label contains a delimiter, the first
     * piece is the external representation and the second is the internal.
     * 
     * @param label The item label.
     * @return The newly created combo item.
     */
    protected Comboitem appendItem(String label) {
        int i = delimiter == null || label == null ? -1 : label.indexOf(delimiter);
        return i == -1 ? appendItem(label, label)
                : appendItem(label.substring(0, i), label.substring(i + delimiter.length()));
    }
    
    /**
     * Append a new combo item using the specified label and internal value.
     * 
     * @param label The item label.
     * @param value The internal value.
     * @return The newly created combo item.
     */
    protected Comboitem appendItem(String label, Object value) {
        Comboitem item = new Comboitem(label);
        editor.addChild(item);
        item.setData(value);
        return item;
    }
    
    @Override
    protected Object getValue() {
        Comboitem item = editor.getSelectedItem();
        Object value = item != null ? item.getData() : editor.isReadonly() ? null : editor.getValue();
        return value;
    }
    
    @Override
    protected void setValue(Object value) {
        Comboitem item = (Comboitem) editor.getChildByData(value);
        editor.setSelectedItem(item);
        
        if (item == null) {
            editor.setValue(value == null ? null : value.toString());
        }
        
        updateValue();
    }
    
    @EventHandler(value = "dblclick", target = "editor")
    private void onDblclick() {
        int i = editor.getSelectedIndex() + 1;
        editor.setSelectedIndex(i >= editor.getChildCount() ? 0 : i);
        onChange(null);
        //TODO: editor.close();
    }
}
