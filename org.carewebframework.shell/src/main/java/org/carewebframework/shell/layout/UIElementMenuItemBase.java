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

import org.carewebframework.ui.zk.MenuEx;
import org.carewebframework.ui.zk.MenuUtil;

import org.zkoss.zul.Menu;

/**
 * Base class for representing a single menu item. The Menu class has been subclassed (MenuEx) to
 * allow it to behave as a menu and a menu item. This simplifies the creation and manipulation of
 * hierarchical menu trees.
 */
public class UIElementMenuItemBase extends UIElementActionBase {
    
    private final Menu menu = new MenuEx();
    
    public UIElementMenuItemBase() {
        super();
        maxChildren = Integer.MAX_VALUE;
    }
    
    public String getLabel() {
        return menu.getLabel();
    }
    
    public void setLabel(String label) {
        menu.setLabel(label);
    }
    
    protected Menu getMenu() {
        return menu;
    }
    
    /**
     * The caption label is the instance name.
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }
    
    @Override
    public void bringToFront() {
        super.bringToFront();
        
        if (isDesignMode()) {
            MenuUtil.open(menu);
        }
    }
    
    @Override
    protected void afterAddChild(UIElementBase child) {
        super.afterAddChild(child);
        MenuUtil.updateStyles(menu);
    }
    
    @Override
    protected void afterRemoveChild(UIElementBase child) {
        super.afterRemoveChild(child);
        MenuUtil.updateStyles(menu);
    }
    
    @Override
    protected void bind() {
        if (getParent() instanceof UIElementMenuItemBase) {
            ((UIElementMenuItemBase) getParent()).menu.getMenupopup().appendChild(menu);
        } else {
            super.bind();
        }
    }
    
    @Override
    protected void unbind() {
        menu.detach();
    }
}
