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
package org.carewebframework.ui.zk;

import org.carewebframework.web.component.Menu;
import org.carewebframework.web.event.ClickEvent;

/**
 * Extends menu component by logically pruning menu items with no visible children.
 */
public class MenuEx extends Menu {
    
    public MenuEx() {
        super();
        //TODO: ZKUtil.setCustomColorLogic(this, "this.setColor(value ? value : '');");
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        adjustVisibility(visible);
    }
    
    /**
     * Adjust visibility of parent menu elements.
     * 
     * @param visible The visibility state.
     */
    private void adjustVisibility(boolean visible) {
        Menu menu = this;
        
        while ((menu = getParentMenu(menu)) != null) {
            visible |= menu.getFirstVisibleChild(false) != null;
            visible |= menu.hasEventListener(ClickEvent.class);
            boolean oldVisible = menu.isVisible();
            
            if (visible == oldVisible) {
                break;
            }
            
            menu.setVisible(visible);
        }
        
        //TODO: MenuUtil.updateStyles(menu == null ? menuPopup : menu);
    }
    
    /**
     * Returns the parent menu for this menu, or null if none.
     * 
     * @param menu Menu whose parent menu is sought.
     * @return The parent menu or null if none.
     */
    private Menu getParentMenu(Menu menu) {
        return menu.getParent() instanceof Menu ? (Menu) menu.getParent() : null;
    }
}
