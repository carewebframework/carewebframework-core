/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.infopanel.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.infopanel.model.IInfoPanel;
import org.carewebframework.ui.zk.MenuUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.LayoutRegion;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Rows;

/**
 * Controller for the main info panel.
 */
public class InfoPanelController extends PluginController implements IInfoPanel {
    
    private static final Log log = LogFactory.getLog(InfoPanelController.class);
    
    private static final long serialVersionUID = 1L;
    
    protected static final String ALERT_ACTION_EVENT = "onAlertAction";
    
    private Menubar menubar;
    
    private Component dropRoot;
    
    private HtmlBasedComponent alertIcon;
    
    private LayoutRegion alertPanel;
    
    private Grid alertGrid;
    
    private Rows alertRoot;
    
    private Label alertTitle;
    
    private String collapsedAlertPanelHeight;
    
    private String openAlertPanelHeight = "33%";
    
    private boolean alertPanelOpen;
    
    private String alertTitlePrefix;
    
    private int lastAlertCount = -1;
    
    /**
     * Listener for event-based drop and alert requests.
     */
    private final IGenericEvent<Component> dropListener = new IGenericEvent<Component>() {
        
        @Override
        public void eventCallback(final String eventName, final Component comp) {
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
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        ((HtmlBasedComponent) comp).setDroppable(getDropId());
        this.collapsedAlertPanelHeight = this.alertPanel.getHeight();
        this.alertTitlePrefix = this.alertTitle.getValue();
        openAlertPanel(false);
    }
    
    /**
     * Adds a menu item to the panel's menu bar.
     * 
     * @param menuitem The menu item to register.
     * @param path The menu path under which the item should appear.
     * @see #org.carewebframework.ui.infopanel.model.IInfoPanel.registerMenuItem(Menuitem, String)
     */
    @Override
    public void registerMenuItem(final Menuitem menuitem, final String path) {
        MenuUtil.addMenuItem(path + "\\" + menuitem.getLabel(), menuitem, this.menubar, null);
        
        if (log.isDebugEnabled()) {
            log.debug("Registered menu item: " + menuitem);
        }
    }
    
    /**
     * Removes a menu item from the panel's menu bar.
     * 
     * @param menuitem Menu item to remove.
     * @see #org.carewebframework.ui.infopanel.model.IInfoPanel.unregisterMenuItem(Menuitem)
     */
    @Override
    public void unregisterMenuItem(final Menuitem menuitem) {
        final Component parent = menuitem.getParent();
        menuitem.detach();
        
        if (parent != null) {
            MenuUtil.pruneMenus(parent);
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
    public void onDrop(final DropEvent event) {
        drop(event.getDragged());
    }
    
    /**
     * Toggles the alert panel open state.
     */
    public void onClick$alertIcon() {
        openAlertPanel(!this.alertPanelOpen);
    }
    
    /**
     * Handles onAlertAction event which an alert container sends after it has processed an action.
     * 
     * @param event
     */
    public void onAlertAction$alertRoot(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Action action = (Action) event.getData();
        openAlertPanel(null);
        
        if (action == Action.TOP) {
            alertGrid.setActivePage(0);
            alertGrid.invalidate();
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
            this.openAlertPanelHeight = this.alertPanel.getHeight();
        }
        
        int alertCount = 0;
        
        for (Component cmp : this.alertRoot.getChildren()) {
            if (cmp.isVisible()) {
                alertCount++;
            }
        }
        
        open = open == null ? alertCount > 0 : open;
        this.alertPanelOpen = open;
        this.alertGrid.setVisible(open);
        this.alertIcon.setStyle(open ? "background-position: 0 -32px" : "");
        this.alertPanel.setMinsize(open ? 70 : 0);
        this.alertPanel.setSplittable(open);
        this.alertPanel.setHeight(open ? this.openAlertPanelHeight : this.collapsedAlertPanelHeight);
        
        if (alertCount != this.lastAlertCount) {
            this.lastAlertCount = alertCount;
            final String countStr = (alertCount == 0 ? "no" : Integer.toString(alertCount)) + " alert"
                    + (alertCount == 1 ? "" : "s");
            this.alertTitle.setValue(this.alertTitlePrefix + " - " + countStr);
            this.alertPanel.setVisible(alertCount > 0);
        }
    }
    
    /**
     * Returns the drop id for the panel.
     * 
     * @see #org.carewebframework.ui.zk.IDropHandler.getDropId()
     */
    @Override
    public String getDropId() {
        return DROP_ID;
    }
    
    /**
     * Drops the specified item onto the panel, invoking its renderer.
     * 
     * @param droppedItem Item to drop.
     * @see #org.carewebframework.ui.zk.IDropHandler.drop(Component)
     */
    @Override
    public void drop(final Component droppedItem) {
        DropContainer.render(this.dropRoot, droppedItem);
    }
    
    /**
     * Displays an alert in the alert grid.
     * 
     * @param root Root component for the rendered alert.
     */
    @Override
    public void showAlert(final Component root) {
        AlertContainer.render(this.alertRoot, root);
        this.alertGrid.setActivePage(0);
        openAlertPanel(true);
    }
    
    /**
     * Clear all alerts.
     */
    @Override
    public void clearAlerts() {
        List<?> children = this.alertRoot.getChildren();
        
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
    public void onLoad(PluginContainer container) {
        getEventManager().subscribe(EVENT_NAME, this.dropListener);
    }
    
    /**
     * Unsubscribe from drop request events.
     */
    @Override
    public void onUnload() {
        getEventManager().unsubscribe(EVENT_NAME, this.dropListener);
    }
    
}
