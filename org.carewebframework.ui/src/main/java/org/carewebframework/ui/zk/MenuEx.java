/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.carewebframework.ui.action.ActionListener;

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
        appendChild(new Menupopup());
        ZKUtil.setCustomColorLogic(this, "this.setColor(value ? value : '');");
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
