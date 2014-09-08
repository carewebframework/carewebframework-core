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

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menupopup;

/**
 * Implements a tab-based menubar. When dropped on a tab pane, provides a drop-down menu on the
 * associated tab. By adding tab menu panes (UIElementTabMenuPane) as children, menu items may be
 * added to to the drop-down menu. Clicking on a menu item activates the associated tab menu pane.
 */
public class UIElementTabMenu extends UIElementMenuBase {
    
    static {
        registerAllowedChildClass(UIElementTabMenu.class, UIElementTabMenuPane.class);
        registerAllowedParentClass(UIElementTabMenu.class, UIElementTabPane.class);
        PropertyTypeRegistry.register("tabmenuitems", null, PropertyEditorTabMenu.class);
    }
    
    private UIElementTabMenuPane activePane;
    
    private final Div paneAnchor = new Div();
    
    public UIElementTabMenu() {
        super(new Menubar(), new Menupopup());
        Menubar menubar = getMenubar();
        menubar.setSclass("cwf-tab-menubar");
        Menu menu = new Menu();
        menubar.appendChild(menu);
        menu.appendChild(getInnerComponent());
        fullSize(paneAnchor);
        paneAnchor.setSclass("cwf-tab-menu");
        associateComponent(paneAnchor);
    }
    
    /**
     * Sets which tab menu pane is currently active.
     * 
     * @param activePane The pane to become active.
     */
    protected void setActivePane(UIElementTabMenuPane activePane) {
        if (this.activePane != null) {
            this.activePane.activate(false);
        }
        
        this.activePane = activePane;
        
        if (this.activePane != null) {
            this.activePane.activate(true);
        }
    }
    
    /*package*/Component getPaneAnchor() {
        return paneAnchor;
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
        tabPane.getCaption().appendChild(getMenubar());
        tabPane.getInnerComponent().appendChild(paneAnchor);
    }
    
    @Override
    protected void unbind() {
        super.unbind();
        paneAnchor.detach();
    }
}
