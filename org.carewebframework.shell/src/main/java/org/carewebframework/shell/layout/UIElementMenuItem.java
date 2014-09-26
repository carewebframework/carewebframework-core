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

import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.ui.zk.MenuUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menupopup;

/**
 * Single menu item. The path determines the position of the menu item in the parent menu tree. Note
 * that we use the Menu class exclusively to represent menu items, not the Menuitem class. The Menu
 * class has been modified to allow it to behave as a menu and a menu item. This simplifies the
 * creation and manipulation of hierarchical menu trees.
 */
public class UIElementMenuItem extends UIElementActionBase {
    
    static {
        registerAllowedParentClass(UIElementMenuItem.class, UIElementMenubar.class);
        registerAllowedParentClass(UIElementMenuItem.class, UIElementMenuItem.class);
        registerAllowedChildClass(UIElementMenuItem.class, UIElementMenuItem.class);
    }
    
    public static class MenuEx extends Menu {
        
        private static final long serialVersionUID = 1L;
        
        public MenuEx() {
            super();
            setWidgetClass("cwf.ext.MenuEx");
            setWidgetOverride(CUSTOM_COLOR_OVERRIDE, "function(value) {this.setColor(value?value:'');}");
            appendChild(new Menupopup());
        }
        
        @Override
        public boolean setVisible(boolean visible) {
            boolean result = super.setVisible(visible);
            adjustVisibility(visible);
            return result;
        }
        
        /**
         * Adjust visibility of parent menu elements.
         * 
         * @param visible The visibility state.
         */
        private void adjustVisibility(boolean visible) {
            Menu menu = this;
            Menupopup menuPopup = null;
            
            while ((menuPopup = getParentPopup(menu)) != null) {
                visible |= ZKUtil.firstVisibleChild(menuPopup, false) != null;
                menu = (Menu) menuPopup.getParent();
                visible |= ActionListener.getListener(menu, Events.ON_CLICK) != null;
                
                if (visible == menu.setVisible(visible)) {
                    break;
                }
            }
            
            if (menu != null) {
                MenuUtil.updateStyles(menu);
            }
        }
        
        /**
         * Returns the parent menu popup for this menu, or null if none.
         * 
         * @param menu Menu whose parent menu popup is sought.
         * @return The parent menu popup or null if none.
         */
        private Menupopup getParentPopup(Menu menu) {
            return menu.isTopmost() ? null : (Menupopup) menu.getParent();
        }
    }
    
    private final Menu menu = new MenuEx();
    
    public UIElementMenuItem() {
        super();
        maxChildren = Integer.MAX_VALUE;
        autoHide = false;
        setOuterComponent(menu);
        setInnerComponent(menu.getMenupopup());
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
        if (getParent() instanceof UIElementMenuItem) {
            ((UIElementMenuItem) getParent()).menu.getMenupopup().appendChild(menu);
        } else {
            super.bind();
        }
    }
    
    @Override
    protected void unbind() {
        menu.detach();
    }
}
