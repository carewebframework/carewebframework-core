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
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

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
    private final IEventListener listener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            MenuUtil.close(getMenu());
            bringToFront();
        }
        
    };
    
    private final Div div = new Div();
    
    public UIElementTabMenuPane() {
        super();
        autoHide = true;
        fullSize(div);
        div.addClass("cwf-tab-menupane");
        setOuterComponent(getMenu());
        setInnerComponent(div);
        div.setVisible(false);
        getMenu().addEventListener(ClickEvent.TYPE, listener);
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
        tabMenu.getPaneAnchor().addChild(div);
    }
    
    @Override
    protected void unbind() {
        super.unbind();
        div.detach();
    }
    
}
