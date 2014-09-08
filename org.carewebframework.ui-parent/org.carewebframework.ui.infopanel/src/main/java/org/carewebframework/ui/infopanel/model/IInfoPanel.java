/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.infopanel.model;

import org.carewebframework.ui.zk.IDropHandler;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Menuitem;

/**
 * Interface for interacting with an information panel.
 */
public interface IInfoPanel extends IDropHandler {
    
    static final String EVENT_NAME = "INFOPANEL";
    
    static final String DROP_EVENT_NAME = EVENT_NAME + ".DROP";
    
    static final String ALERT_EVENT_NAME = EVENT_NAME + ".ALERT";
    
    static final String DROP_ID = "infopanel";
    
    enum Action {
        REMOVE, HIDE, SHOW, COLLAPSE, EXPAND, TOP
    };
    
    /**
     * Registers a menu item with the drop panel using the specified menu path.
     * 
     * @param menuitem The menu item.
     * @param path The menu path.
     */
    void registerMenuItem(Menuitem menuitem, String path);
    
    /**
     * Unregisters a menu item from the drop panel. Any empty parent menus will be automatically
     * removed. Calling this method is preferable to detaching the menu item directly since it
     * allows for proper maintenance of parent menu items.
     * 
     * @param menuItem The menu item.
     */
    void unregisterMenuItem(Menuitem menuItem);
    
    /**
     * Displays an alert.
     * 
     * @param root The root component.
     */
    void showAlert(Component root);
    
    /**
     * Clears all alerts.
     */
    void clearAlerts();
    
}
