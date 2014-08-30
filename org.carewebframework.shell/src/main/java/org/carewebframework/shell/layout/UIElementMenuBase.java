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

import org.carewebframework.ui.zk.MenuUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Menubar;

/**
 * Base implementation of a menu bar.
 */
public class UIElementMenuBase extends UIElementZKBase {
    
    private final Menubar menubar;
    
    public UIElementMenuBase(Menubar menubar) {
        this(menubar, menubar);
    }
    
    public UIElementMenuBase(Menubar menubar, Component root) {
        super();
        this.menubar = menubar;
        menubar.setSclass("cwf-menubar");
        setOuterComponent(menubar);
        setInnerComponent(root);
        maxChildren = Integer.MAX_VALUE;
    }
    
    /**
     * Returns a reference to the menu bar.
     * 
     * @return The menu bar.
     */
    public Menubar getMenubar() {
        return menubar;
    }
    
    @Override
    protected void afterAddChild(UIElementBase child) {
        super.afterAddChild(child);
        updateMenubar();
    }
    
    @Override
    protected void afterRemoveChild(UIElementBase child) {
        super.afterRemoveChild(child);
        updateMenubar();
    }
    
    protected void updateMenubar() {
        MenuUtil.updateStyles(menubar);
        Clients.resize(menubar);
    }
}
