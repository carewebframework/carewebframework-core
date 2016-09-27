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

import org.carewebframework.ui.zk.MenuEx;
import org.carewebframework.ui.zk.MenuUtil;

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
