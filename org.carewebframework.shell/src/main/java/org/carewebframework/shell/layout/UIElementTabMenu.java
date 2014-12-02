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

import org.carewebframework.shell.designer.PropertyEditorTabMenu;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.zk.MenuUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Tab;

/**
 * Implements a tab-based menubar. When dropped on a tab pane, provides a drop-down menu on the
 * associated tab. By adding tab menu panes (UIElementTabMenuPane) as children, menu items may be
 * added to to the drop-down menu. Clicking on a menu item activates the associated tab menu pane.
 */
public class UIElementTabMenu extends UIElementZKBase {
    
    static {
        registerAllowedChildClass(UIElementTabMenu.class, UIElementTabMenuPane.class);
        registerAllowedParentClass(UIElementTabMenu.class, UIElementTabPane.class);
        PropertyTypeRegistry.register("tabmenuitems", PropertyEditorTabMenu.class);
    }
    
    public static class MenupopupEx extends Menupopup {
        
        private static final long serialVersionUID = 1L;
        
        private Tab tab;
        
        public void onOpenMenu() {
            open(tab, "after_start");
        }
        
        @Override
        public void open(Component ref, String position) {
            ref = ref == null ? tab : ref;
            
            if (getPage() == null) {
                setPage(ref.getPage());
            }
            
            MenuUtil.updateStyles(this);
            super.open(ref, position);
        }
        
        @Override
        public void onChildAdded(Component child) {
            super.onChildAdded(child);
            tab.setClosable(true);
        }
        
        @Override
        public void onChildRemoved(Component child) {
            super.onChildRemoved(child);
            tab.setClosable(getFirstChild() != null);
        }
        
        /**
         * Sets the associated tab and captures its onClose event to open the popup.
         * 
         * @param tab Tab to associate.
         */
        public void setTab(Tab tab) {
            if (this.tab != null) {
                this.tab.removeForward("onClose", this, "onOpenMenu");
                this.tab.setClosable(false);
            }
            
            this.tab = tab;
            
            if (this.tab != null) {
                this.tab.addForward("onClose", this, "onOpenMenu");
                this.tab.setClosable(getFirstChild() != null);
            }
            
        }
    }
    
    private UIElementTabMenuPane activePane;
    
    private final Div paneAnchor = new Div();
    
    private final MenupopupEx menupopup = new MenupopupEx();
    
    public UIElementTabMenu() {
        super();
        setOuterComponent(menupopup);
        fullSize(paneAnchor);
        paneAnchor.setSclass("cwf-tab-menu");
        associateComponent(paneAnchor);
        maxChildren = Integer.MAX_VALUE;
    }
    
    /**
     * Sets which tab menu pane is currently active.
     * 
     * @param activePane The pane to become active.
     */
    protected void setActivePane(UIElementTabMenuPane activePane) {
        if (this.activePane != null) {
            this.activePane.activate(false);
            updateMenuStyle(this.activePane.getMenu(), false);
        }
        
        this.activePane = activePane;
        
        if (this.activePane != null) {
            this.activePane.activate(true);
            updateMenuStyle(this.activePane.getMenu(), true);
        }
    }
    
    private void updateMenuStyle(Menu menu, boolean selected) {
        ZKUtil.toggleSclass(menu, "cwf-tab-menu-seld", "cwf-tab-menu", selected);
    }
    
    /*package*/Component getPaneAnchor() {
        return paneAnchor;
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        // do nothing
    }
    
    /**
     * Only one child, the active menu pane, can be active at a time. If there is no active menu
     * pane, activate the first one.
     */
    @Override
    protected void activateChildren(boolean activate) {
        if (activePane == null) {
            activePane = (UIElementTabMenuPane) getFirstChild();
        }
        
        if (activePane != null) {
            activePane.activate(activate);
        }
    }
    
    @Override
    protected void setDesignContextMenu(Menupopup contextMenu) {
        super.setDesignContextMenu(contextMenu);
        setDesignContextMenu(paneAnchor, contextMenu);
    }
    
    @Override
    protected void bind() {
        UIElementTabPane tabPane = (UIElementTabPane) getParent();
        menupopup.setTab(tabPane.getTab());
        tabPane.getInnerComponent().appendChild(paneAnchor);
    }
    
    @Override
    protected void unbind() {
        super.unbind();
        paneAnchor.detach();
        menupopup.setTab(null);
        menupopup.detach();
    }
}
