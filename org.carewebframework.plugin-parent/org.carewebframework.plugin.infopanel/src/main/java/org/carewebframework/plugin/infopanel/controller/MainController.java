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
package org.carewebframework.plugin.infopanel.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.plugin.infopanel.model.IInfoPanel;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.util.MenuUtil;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Grid;
import org.fujion.component.Label;
import org.fujion.component.Menuitem;
import org.fujion.component.Rows;
import org.fujion.component.Toolbar;
import org.fujion.event.DropEvent;
import org.fujion.event.Event;

/**
 * Controller for the main info panel.
 */
public class MainController extends PluginController implements IInfoPanel {
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    protected static final String ALERT_ACTION_EVENT = "alertAction";
    
    @WiredComponent
    private Toolbar menubar;
    
    @WiredComponent
    private BaseComponent dropRoot;
    
    @WiredComponent
    private BaseUIComponent alertIcon;
    
    @WiredComponent
    private BaseUIComponent alertPanel;
    
    @WiredComponent
    private BaseUIComponent menuPanel;
    
    @WiredComponent
    private Grid alertGrid;
    
    @WiredComponent
    private Rows alertRoot;
    
    @WiredComponent
    private Label alertTitle;
    
    private String collapsedAlertPanelHeight;
    
    private String openAlertPanelHeight = "33%";
    
    private boolean alertPanelOpen;
    
    private String alertTitlePrefix;
    
    private int lastAlertCount = -1;
    
    /**
     * Listener for event-based drop and alert requests.
     */
    private final IGenericEvent<BaseComponent> dropListener = new IGenericEvent<BaseComponent>() {
        
        @Override
        public void eventCallback(String eventName, BaseComponent comp) {
            if (isActive()) {
                if (eventName.equals(DROP_EVENT_NAME)) {
                    drop(comp);
                } else if (eventName.equals(ALERT_EVENT_NAME)) {
                    showAlert(comp);
                }
            }
        }
        
    };
    
    /**
     * Set the drop id of the root component.
     * 
     * @param comp The root component of the info panel.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        ((BaseUIComponent) comp).setDropid(getDropId());
        this.collapsedAlertPanelHeight = alertPanel.getHeight();
        this.alertTitlePrefix = this.alertTitle.getLabel();
        openAlertPanel(false);
    }
    
    /**
     * Adds a menu item to the panel's menu bar.
     * 
     * @param menuitem The menu item to register.
     * @param path The menu path under which the item should appear.
     * @see org.carewebframework.plugin.infopanel.model.IInfoPanel#registerMenuItem(Menuitem,
     *      String)
     */
    @Override
    public void registerMenuItem(Menuitem menuitem, String path) {
        MenuUtil.addMenuOrMenuItem(path + "\\" + menuitem.getLabel(), menuitem, menubar, null);
        menuPanel.setVisible(true);
        
        if (log.isDebugEnabled()) {
            log.debug("Registered menu item: " + menuitem);
        }
    }
    
    /**
     * Removes a menu item from the panel's menu bar.
     * 
     * @param menuitem Menu item to remove.
     * @see org.carewebframework.plugin.infopanel.model.IInfoPanel#unregisterMenuItem(Menuitem)
     */
    @Override
    public void unregisterMenuItem(Menuitem menuitem) {
        BaseComponent parent = menuitem.getParent();
        menuitem.detach();
        
        if (parent != null) {
            MenuUtil.pruneMenus(parent);
            menuPanel.setVisible(parent.getFirstChild() != null);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Unregistered menu item: " + menuitem);
        }
        
    }
    
    /**
     * Handler for drop events.
     * 
     * @param event The drop event.
     */
    public void onDrop(DropEvent event) {
        drop(event.getRelatedTarget());
    }
    
    /**
     * Toggles the alert panel open state.
     */
    public void onClick$alertIcon() {
        openAlertPanel(!alertPanelOpen);
    }
    
    /**
     * Handles onAlertAction event which an alert container sends after it has processed an action.
     * 
     * @param event The onAlertAction event.
     */
    public void onAlertAction$alertRoot(Event event) {
        Action action = (Action) event.getData();
        openAlertPanel(null);
        
        if (action == Action.TOP) {
            //alertGrid.setActivePage(0);
        }
    }
    
    /**
     * Open or close the alert panel, updating the alert icon.
     * 
     * @param open If true, open the alert panel. If false, close it. If null, open status is
     *            determined by the presence or absence of alerts.
     */
    private void openAlertPanel(Boolean open) {
        if (this.alertPanelOpen) {
            this.openAlertPanelHeight = alertPanel.getHeight();
        }
        
        int alertCount = 0;
        
        for (BaseUIComponent cmp : alertRoot.getChildren(BaseUIComponent.class)) {
            if (cmp.isVisible()) {
                alertCount++;
            }
        }
        
        open = open == null ? alertCount > 0 : open;
        alertPanelOpen = open;
        alertGrid.setVisible(open);
        //alertPanel.setMinsize(open ? 70 : 0);
        //alertPanel.setSplittable(open);
        alertPanel.setHeight(open ? this.openAlertPanelHeight : this.collapsedAlertPanelHeight);
        alertIcon.addClass(open ? "chevron:glyphicon-chevron-down" : "chevron:glyphicon-chevron-up");
        
        if (alertCount != lastAlertCount) {
            lastAlertCount = alertCount;
            String countStr = (alertCount == 0 ? "no" : Integer.toString(alertCount)) + " alert"
                    + (alertCount == 1 ? "" : "s");
            alertTitle.setLabel(alertTitlePrefix + " - " + countStr);
            alertPanel.setVisible(alertCount > 0);
        }
    }
    
    /**
     * Returns the drop id for the panel.
     */
    @Override
    public String getDropId() {
        return DROP_ID;
    }
    
    /**
     * Drops the specified item onto the panel, invoking its renderer.
     * 
     * @param droppedItem Item to drop.
     */
    @Override
    public void drop(BaseComponent droppedItem) {
        DropContainer.render(dropRoot, droppedItem);
    }
    
    /**
     * Displays an alert in the alert grid.
     * 
     * @param root Root component for the rendered alert.
     */
    @Override
    public void showAlert(BaseComponent root) {
        AlertContainer.render(alertRoot, root);
        //alertGrid.setActivePage(0);
        openAlertPanel(true);
    }
    
    /**
     * Clear all alerts.
     */
    @Override
    public void clearAlerts() {
        List<BaseComponent> children = alertRoot.getChildren();
        
        while (children.size() > 0) {
            AlertContainer alertContainer = (AlertContainer) children.get(0);
            alertContainer.doAction(Action.REMOVE);
        }
        
        openAlertPanel(false);
    }
    
    /**
     * Subscribe to drop request events.
     */
    @Override
    public void onLoad(ElementPlugin plugin) {
        super.onLoad(plugin);
        getEventManager().subscribe(EVENT_NAME, dropListener);
    }
    
    /**
     * Unsubscribe from drop request events.
     */
    @Override
    public void onUnload() {
        super.onUnload();
        getEventManager().unsubscribe(EVENT_NAME, dropListener);
    }
    
}
