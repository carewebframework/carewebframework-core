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
import org.carewebframework.ui.zk.ListUtil;

import org.springframework.util.StringUtils;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

/**
 * Base class for combobox-based property editors.
 */
public class PropertyEditorList extends PropertyEditorBase {
    
    protected final Combobox combobox;
    
    private String delimiter;
    
    private final EventListener<KeyEvent> deleteListener = new EventListener<KeyEvent>() {
        
        /**
         * Pressing delete key or control-X will clear combo box selection.
         * 
         * @param event The control key event.
         * @throws Exception Unspecified exception.
         */
        @Override
        public void onEvent(KeyEvent event) throws Exception {
            if (event.getKeyCode() == KeyEvent.DELETE || event.getKeyCode() == 88) {
                boolean changed = !StringUtils.isEmpty(combobox.getValue());
                combobox.setValue(null);
                combobox.close();
                
                if (changed) {
                    Events.postEvent(Events.ON_CHANGE, combobox, null);
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
            combobox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
        }
        
        combobox.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener<Event>() {
            
            /**
             * Double-clicking a combo item will select the item and close the combo box.
             * 
             * @param event The double click event.
             * @throws Exception Unspecified exception.
             */
            @Override
            public void onEvent(Event event) throws Exception {
                int i = combobox.getSelectedIndex() + 1;
                combobox.setSelectedIndex(i >= combobox.getItemCount() ? 0 : i);
                Events.sendEvent(Events.ON_CHANGE, propGrid, null);
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
        Comboitem item = combobox.appendItem(label);
        item.setValue(value);
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
