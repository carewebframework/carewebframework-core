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
package org.carewebframework.shell.layout;

import org.carewebframework.shell.designer.PropertyEditorTabMenu;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Menu;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Tab;

/**
 * Implements a tab-based menubar. When dropped on a tab pane, provides a drop-down menu on the
 * associated tab. By adding tab menu panes (UIElementTabMenuPane) as children, menu items may be
 * added to to the drop-down menu. Clicking on a menu item activates the associated tab menu pane.
 */
public class UIElementTabMenu extends UIElementCWFBase {
    
    static {
        registerAllowedChildClass(UIElementTabMenu.class, UIElementTabMenuPane.class);
        registerAllowedParentClass(UIElementTabMenu.class, UIElementTabPane.class);
        PropertyTypeRegistry.register("tabmenuitems", PropertyEditorTabMenu.class);
    }
    
    public static class MenupopupEx extends Menupopup {
        
        private Tab tab;
        
        /*TODO:
        public void onOpenMenu() {
            open(tab, "after_start");
        }
        
        @Override
        public void open(BaseComponent ref, String position) {
            ref = ref == null ? tab : ref;
            
            if (getPage() == null) {
                setParent(ref.getPage());
            }
            
            super.open(ref, position);
        }
        */
        
        @Override
        public void afterAddChild(BaseComponent child) {
            super.afterAddChild(child);
            tab.setClosable(true);
        }
        
        @Override
        public void afterRemoveChild(BaseComponent child) {
            super.afterRemoveChild(child);
            tab.setClosable(getFirstChild() != null);
        }
        
        /**
         * Sets the associated tab and captures its onClose event to open the popup.
         * 
         * @param tab Tab to associate.
         */
        public void setTab(Tab tab) {
            if (this.tab != null) {
                this.tab.removeEventForward("onClose", this, "onOpenMenu");
                this.tab.setClosable(false);
            }
            
            this.tab = tab;
            
            if (this.tab != null) {
                this.tab.addEventForward("onClose", this, "onOpenMenu");
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
        paneAnchor.addClass("cwf-tab-menu");
        associateComponent(paneAnchor);
        maxChildren = Integer.MAX_VALUE;
        setMaskMode(null);
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
        menu.toggleClass("cwf-tab-menu-seld", "cwf-tab-menu", selected);
    }
    
    /*package*/BaseComponent getPaneAnchor() {
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
        tabPane.getInnerComponent().addChild(paneAnchor);
    }
    
    @Override
    protected void unbind() {
        super.unbind();
        paneAnchor.detach();
        menupopup.setTab(null);
        menupopup.detach();
    }
}
