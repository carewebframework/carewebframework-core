/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import org.carewebframework.shell.designer.PropertyEditorMenubar;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zul.Menubar;

/**
 * Implements a shared menubar.
 */
public class UIElementMenubar extends UIElementMenuBase {
    
    static {
        registerAllowedChildClass(UIElementMenubar.class, UIElementMenuItem.class);
        registerAllowedParentClass(UIElementMenubar.class, UIElementBase.class);
        PropertyTypeRegistry.register("menuitems", null, PropertyEditorMenubar.class);
    }
    
    /**
     * Creates the menu bar UI element. This consists of a ZK menu bar component. A help menu is
     * automatically created a pre-populated with references to the about dialog and table of
     * contents. We also attach an onOpen event handler to the help menu and use this to do
     * just-in-time sorting of dynamically added items.
     */
    public UIElementMenubar() {
        this(new Menubar());
    }
    
    public UIElementMenubar(Menubar menubar) {
        super(menubar);
    }
    
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        ZKUtil.updateStyle(getMenubar(), "min-width", designMode ? "40px" : null);
    }
    
}
