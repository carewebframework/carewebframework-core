/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.icons;

import org.carewebframework.ui.zk.IconPicker;
import org.carewebframework.ui.zk.ListUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Toolbar;

/**
 * Extends the icon picker by adding the ability to pick an icon library from which to choose.
 */
public class IconPickerEx extends IconPicker {
    
    private static final long serialVersionUID = 1L;
    
    private IIconLibrary iconLibrary;
    
    private final Combobox cboLibrary;
    
    private final IconLibraryRegistry iconRegistry = IconLibraryRegistry.getInstance();
    
    private boolean selectorVisible;
    
    private String dimensions = "16x16";
    
    public IconPickerEx() {
        super();
        cboLibrary = new Combobox();
        cboLibrary.setWidth("100%");
        cboLibrary.setReadonly(true);
        cboLibrary.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                iconLibrary = (IIconLibrary) cboLibrary.getSelectedItem().getValue();
                libraryChanged();
            }
            
        });
        
        Toolbar tb = new Toolbar();
        panel.addToolbar("tbar", tb);
        tb.appendChild(cboLibrary);
        
        for (IIconLibrary lib : iconRegistry) {
            Comboitem item = new Comboitem(lib.getId());
            item.setValue(lib);
            cboLibrary.appendChild(item);
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
        cboLibrary.setVisible(visible && cboLibrary.getItemCount() > 1);
    }
    
    private void libraryChanged() {
        Events.postEvent("onLibraryChanged", this, iconLibrary);
    }
    
    public void onLibraryChanged() {
        clear();
        addIconsByUrl(iconLibrary.getMatching("*", dimensions));
    }
    
}
