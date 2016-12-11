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
import org.carewebframework.ui.icons.IconLibraryRegistry;
import org.carewebframework.ui.icons.IconPickerEx;
import org.carewebframework.ui.icons.IconUtil;
import org.carewebframework.ui.zk.IconPicker;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Image;

/**
 * Property editor for icon properties. If the associated property has defined choices, the icon
 * picker will be limited to those values only. Otherwise, all registered icons will be available.
 */
public class PropertyEditorIcon extends PropertyEditorBase<IconPickerEx> {
    
    public PropertyEditorIcon() {
        super(new IconPickerEx());
        editor.setAutoAdd(false);
        editor.addEventListener("onSetValue", new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                Object value = event.getData();
                Image icon = value == null ? null : editor.findIcon(value.toString());
                editor.setSelectedItem(icon);
                updateValue();
            }
            
        });
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        editor.addForward(IconPicker.ON_SELECT_ITEM, propGrid, Events.ON_CHANGE);
        String[] values = propInfo.getConfigValueArray("values");
        
        if (values == null) {
            String dflt = IconLibraryRegistry.getInstance().getDefaultLibrary();
            editor.setIconLibrary(dflt);
        } else {
            editor.setSelectorVisible(false);
            
            for (String choice : values) {
                if (choice.startsWith("~./")) {
                    editor.addIconByUrl(choice);
                } else {
                    String[] pcs = choice.split("\\:", 3);
                    String library = pcs.length == 0 ? null : pcs[0];
                    String name = pcs.length < 2 ? "*" : pcs[1];
                    String dimension = pcs.length < 3 ? null : pcs[2];
                    editor.addIconsByUrl(IconUtil.getMatching(library, name, dimension));
                }
            }
        }
    }
    
    @Override
    protected String getValue() {
        Image icon = editor.getSelectedItem();
        return icon == null ? null : icon.getSrc();
    }
    
    @Override
    protected void setValue(Object value) {
        Events.postEvent("onSetValue", editor, value);
    }
}
