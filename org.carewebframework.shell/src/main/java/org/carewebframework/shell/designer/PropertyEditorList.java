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
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.KeyCode;
import org.carewebframework.web.event.KeyEvent;
import org.springframework.util.StringUtils;

/**
 * Base class for combobox-based property editors.
 */
public class PropertyEditorList extends PropertyEditorBase {
    
    protected final Combobox combobox;
    
    private String delimiter;
    
    private final IEventListener deleteListener = new IEventListener() {
        
        /**
         * Pressing delete key or control-X will clear combo box selection.
         * 
         * @param event The control key event.
         * @throws Exception Unspecified exception.
         */
        @Override
        public void onEvent(Event event) {
            KeyEvent evt = (KeyEvent) event;
            
            if (evt.getKeyCode() == KeyCode.VK_DELETE || evt.getKeyCode() == 88) {
                boolean changed = !StringUtils.isEmpty(combobox.getValue());
                combobox.setValue(null);
                combobox.close();
                
                if (changed) {
                    EventUtil.post(ChangeEvent.TYPE, combobox, null);
                }
            }
        }
        
    };
    
    /**
     * Create property editor.
     */
    public PropertyEditorList() {
        super(new Combobox());
        combobox = (Combobox) component;
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, final PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        setReadonly(propInfo.getConfigValueBoolean("readonly", true));
        delimiter = propInfo.getConfigValue("delimiter");
        
        if (!combobox.isReadonly()) {
            combobox.registerEventForward(ChangeEvent.TYPE, propGrid, null);
        }
        
        combobox.addEventListener(DblclickEvent.TYPE, new IEventListener() {
            
            /**
             * Double-clicking a combo item will select the item and close the combo box.
             * 
             * @param event The double click event.
             * @throws Exception Unspecified exception.
             */
            @Override
            public void onEvent(Event event) {
                int i = combobox.getSelectedIndex() + 1;
                combobox.setSelectedIndex(i >= combobox.getChildCount() ? 0 : i);
                EventUtil.send(ChangeEvent.TYPE, propGrid, null);
                combobox.close();
            }
            
        });
    }
    
    /**
     * Set read only mode.
     * 
     * @param readonly If true, the combobox is read only and the delete key (or ctrl-x) will remove
     *            the selection. If false, the combo box content can be modified and the delete key
     *            functions normally.
     */
    private void setReadonly(boolean readonly) {
        combobox.setReadonly(readonly);
        combobox.setCtrlKeys(readonly ? "#del^x" : null);
        
        if (readonly) {
            combobox.addEventListener(Events.ON_CTRL_KEY, deleteListener);
        } else {
            combobox.removeEventListener(Events.ON_CTRL_KEY, deleteListener);
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
        combobox.addChild(item);
        item.setData(value);
        return item;
    }
    
    @Override
    protected Object getValue() {
        Comboitem item = combobox.getSelectedItem();
        Object value = item != null ? item.getValue() : combobox.isReadonly() ? null : combobox.getValue();
        return value;
    }
    
    @Override
    protected void setValue(Object value) {
        int index = value == null ? -1 : ListUtil.selectComboboxData(combobox, value);
        
        if (index < 0) {
            combobox.setValue(value == null ? null : value.toString());
        }
        
        updateValue();
    }
}
