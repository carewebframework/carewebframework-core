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
package org.carewebframework.ui.dialog;

import java.util.List;

import org.carewebframework.ui.Constants;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.OnFailure;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Combobox;
import org.fujion.component.Comboitem;
import org.fujion.component.ImagePicker;
import org.fujion.component.ImagePicker.ImagePickeritem;
import org.fujion.component.Namespace;
import org.fujion.event.ChangeEvent;
import org.fujion.icon.IIconLibrary;
import org.fujion.icon.IconLibraryRegistry;
import org.fujion.page.PageUtil;

/**
 * Extends the icon picker by adding the ability to pick an icon library from which to choose.
 */
public class IconPicker extends Namespace {
    
    private final IconLibraryRegistry iconRegistry = IconLibraryRegistry.getInstance();
    
    @WiredComponent
    private Combobox cboLibrary;
    
    @WiredComponent
    private ImagePicker imgPicker;

    private IIconLibrary iconLibrary;

    private boolean selectorVisible;
    
    private String dimensions = "16x16";
    
    public IconPicker() {
        addStyle("overflow", "visible");
        addStyle("display", "inline-block");
        PageUtil.createPage(Constants.RESOURCE_PREFIX + "cwf/iconPicker.fsp", this);
        wireController(this);
        
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
        Comboitem item = (Comboitem) cboLibrary.findChildByData(iconLibrary);
        
        if (item != null) {
            cboLibrary.setSelectedItem(item);
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
        cboLibrary.setVisible(visible && cboLibrary.getChildCount() > 1);
    }
    
    public String getValue() {
        return imgPicker.getValue();
    }
    
    public void setValue(String value) {
        imgPicker.setValue(value);
    }
    
    public ImagePicker getImagePicker() {
        return imgPicker;
    }
    
    private void libraryChanged() {
        imgPicker.clear();
        imgPicker.destroyChildren();
        imgPicker.addChild(new ImagePickeritem());
        
        for (String lib : iconLibrary.getMatching("*", dimensions)) {
            imgPicker.addChild(new ImagePickeritem(lib));
        }

        fireEvent(ChangeEvent.TYPE);
    }
    
    public void addIconByUrl(String url) {
        ImagePickeritem item = new ImagePickeritem(url);
        imgPicker.addChild(item);
    }
    
    public void addIconsByUrl(List<String> urls) {
        for (String url : urls) {
            addIconByUrl(url);
        }
    }
    
    @EventHandler(value = "change", target = "@imgPicker", onFailure = OnFailure.IGNORE)
    private void onChange$imgPicker(ChangeEvent event) {
        fireEvent(event);
    }
    
    @EventHandler(value = "change", target = "@cboLibrary", onFailure = OnFailure.IGNORE)
    private void onChange$cboLibrary() {
        iconLibrary = (IIconLibrary) cboLibrary.getSelectedItem().getData();
        libraryChanged();
    }
}
