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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menupopup;

/**
 * Extends ZK menu component by logically pruning menu items with no visible children.
 */
public class MenuEx extends Menu {
    
    private static final long serialVersionUID = 1L;
    
    public MenuEx() {
        super();
        ZKUtil.setCustomColorLogic(this, "this.setColor(value ? value : '');");
    }
    
    @Override
    public boolean setVisible(boolean visible) {
        boolean result = super.setVisible(visible);
        adjustVisibility(visible);
        return result;
    }
    
    @Override
    public Menupopup getMenupopup() {
        Menupopup menupopup = super.getMenupopup();
        
        if (menupopup == null) {
            menupopup = new Menupopup();
            appendChild(menupopup);
        }
        
        return menupopup;
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
            
            if (menuPopup.getParent() == null) {
                break;
            }
            
            menu = (Menu) menuPopup.getParent();
            visible |= menu.isListenerAvailable(Events.ON_CLICK, false);
            
            if (visible == menu.setVisible(visible)) {
                break;
            }
        }
        
        MenuUtil.updateStyles(menu == null ? menuPopup : menu);
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
