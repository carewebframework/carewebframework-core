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
import org.carewebframework.ui.icon.IconLibraryRegistry;
import org.carewebframework.ui.icon.IconPicker;
import org.carewebframework.ui.icon.IconUtil;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;

/**
 * Property editor for icon properties. If the associated property has defined choices, the icon
 * picker will be limited to those values only. Otherwise, all registered icons will be available.
 */
public class PropertyEditorIcon extends PropertyEditorBase {
    
    private final IconPicker iconPicker;
    
    public PropertyEditorIcon() {
        super(new IconPicker());
        iconPicker = (IconPicker) component;
        iconPicker.addEventListener("setValue", new IEventListener() {
            
            @Override
            public void onEvent(Event event) {
                Object value = event.getData();
                iconPicker.setValue((String) value);
                updateValue();
            }
            
        });
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        component.addEventForward(ChangeEvent.TYPE, propGrid.getWindow(), null);
        String[] values = propInfo.getConfigValueArray("values");
        
        if (values == null) {
            String dflt = IconLibraryRegistry.getInstance().getDefaultLibrary();
            iconPicker.setIconLibrary(dflt);
        } else {
            iconPicker.setSelectorVisible(false);
            
            for (String choice : values) {
                if (choice.startsWith("web/")) {
                    iconPicker.addIconByUrl(choice);
                } else {
                    String[] pcs = choice.split("\\:", 3);
                    String library = pcs.length == 0 ? null : pcs[0];
                    String name = pcs.length < 2 ? "*" : pcs[1];
                    String dimension = pcs.length < 3 ? null : pcs[2];
                    iconPicker.addIconsByUrl(IconUtil.getMatching(library, name, dimension));
                }
            }
        }
    }
    
    @Override
    protected String getValue() {
        return iconPicker.getValue();
    }
    
    @Override
    protected void setValue(Object value) {
        EventUtil.post("setValue", iconPicker, value);
    }
}
