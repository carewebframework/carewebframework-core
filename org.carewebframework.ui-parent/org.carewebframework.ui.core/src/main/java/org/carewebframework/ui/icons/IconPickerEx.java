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
package org.carewebframework.ui.icons;

import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.component.ImagePicker;
import org.carewebframework.web.component.Toolbar;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.SelectEvent;

/**
 * Extends the icon picker by adding the ability to pick an icon library from which to choose.
 */
public class IconPickerEx extends ImagePicker {
    
    private IIconLibrary iconLibrary;
    
    private final Combobox cboLibrary;
    
    private final Toolbar toolbar;
    
    private final IconLibraryRegistry iconRegistry = IconLibraryRegistry.getInstance();
    
    private boolean selectorVisible;
    
    private String dimensions = "16x16";
    
    public IconPickerEx() {
        super();
        cboLibrary = new Combobox();
        cboLibrary.setWidth("100%");
        cboLibrary.setReadonly(true);
        cboLibrary.registerEventListener(SelectEvent.TYPE, new IEventListener() {
            
            @Override
            public void onEvent(Event event) {
                iconLibrary = (IIconLibrary) cboLibrary.getSelectedItem().getData();
                libraryChanged();
            }
            
        });
        
        toolbar = new Toolbar();
        panel.addToolbar("tbar", toolbar);
        toolbar.addChild(cboLibrary);
        
        for (IIconLibrary lib : iconRegistry) {
            Comboitem item = new Comboitem(lib.getId());
            item.setData(lib);
            cboLibrary.addChild(item);
        }
        
        setSelectorVisible(true);
    }
    
    public IIconLibrary getIconLibrary() {
        return iconLibrary;
    }
    
    public void setIconLibrary(String iconLibrary) {
        setIconLibrary(iconRegistry.get(iconLibrary));
    }
    
    public void setIconLibrary(IIconLibrary iconLibrary) {
        this.iconLibrary = iconLibrary;
        
        if (ListUtil.selectComboboxData(cboLibrary, iconLibrary) != -1) {
            libraryChanged();
        }
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    
    public boolean isSelectorVisible() {
        return selectorVisible;
    }
    
    public void setSelectorVisible(boolean visible) {
        selectorVisible = visible;
        toolbar.setVisible(visible && cboLibrary.getChildCount() > 1);
    }
    
    private void libraryChanged() {
        EventUtil.post("onLibraryChanged", this, iconLibrary);
    }
    
    public void onLibraryChanged() {
        clear();
        destroyChildren();
        
        for (String lib : iconLibrary.getMatching("*", dimensions)) {
            addChild(new Imagepickeritem(lib));
        }
    }
    
}
