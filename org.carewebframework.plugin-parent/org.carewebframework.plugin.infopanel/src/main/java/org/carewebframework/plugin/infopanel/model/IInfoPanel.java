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
package org.carewebframework.plugin.infopanel.model;

import org.fujion.component.BaseComponent;
import org.fujion.component.Menuitem;
import org.fujion.dragdrop.IDropHandler;

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
    void showAlert(BaseComponent root);
    
    /**
     * Clears all alerts.
     */
    void clearAlerts();
    
}
