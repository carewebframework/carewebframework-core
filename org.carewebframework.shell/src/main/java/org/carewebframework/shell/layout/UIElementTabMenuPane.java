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

import org.carewebframework.ui.zk.MenuUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;

/**
 * Single menu item for the tab menu.
 */
public class UIElementTabMenuPane extends UIElementMenuItemBase {
    
    static {
        registerAllowedParentClass(UIElementTabMenuPane.class, UIElementTabMenu.class);
        registerAllowedParentClass(UIElementTabMenuPane.class, UIElementTabMenuPane.class);
        registerAllowedChildClass(UIElementTabMenuPane.class, UIElementBase.class);
    }
    
    /**
     * onClick listener for the associated menu. Action is to activate the associated tab menu pane.
     */
    private final EventListener<Event> listener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            MenuUtil.close(getMenu());
            bringToFront();
        }
        
    };
    
    private final Div div = new Div();
    
    public UIElementTabMenuPane() {
        super();
        autoHide = true;
        fullSize(div);
        div.setSclass("cwf-tab-menupane");
        setOuterComponent(getMenu());
        setInnerComponent(div);
        div.setVisible(false);
        getMenu().addEventListener(Events.ON_CLICK, listener);
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        div.setVisible(visible && activated);
        getMenu().setVisible(visible);
    }
    
    /**
     * Apply/remove the design context menu both the menu item and its associated pane.
     * 
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    @Override
    protected void setDesignContextMenu(Menupopup contextMenu) {
        super.setDesignContextMenu(contextMenu);
        setDesignContextMenu(div, contextMenu);
    }
    
    @Override
    public void bringToFront() {
        super.bringToFront();
        getAncestor(UIElementTabMenu.class).setActivePane(this);
    }
    
    @Override
    protected void applyColor() {
        super.applyColor();
        applyColor(div);
    }
    
    @Override
    protected void bind() {
        super.bind();
        UIElementTabMenu tabMenu = getAncestor(UIElementTabMenu.class);
        tabMenu.getPaneAnchor().appendChild(div);
    }
    
    @Override
    protected void unbind() {
        super.unbind();
        div.detach();
    }
    
}
