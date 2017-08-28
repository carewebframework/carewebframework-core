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
package org.carewebframework.plugin.infopanel;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.plugin.infopanel.model.IInfoPanel;
import org.carewebframework.plugin.infopanel.model.IInfoPanel.Action;
import org.carewebframework.plugin.infopanel.service.InfoPanelService;
import org.carewebframework.ui.controller.FrameworkController;
import org.fujion.component.BaseComponent;
import org.fujion.component.Button;
import org.fujion.component.Checkbox;
import org.fujion.component.Div;
import org.fujion.component.Import;
import org.fujion.component.Label;
import org.fujion.component.Listbox;
import org.fujion.component.Listitem;
import org.fujion.component.Menuitem;
import org.fujion.dragdrop.DropUtil;
import org.fujion.event.ClickEvent;
import org.fujion.event.DblclickEvent;

/**
 * Controller for the test component. This component provides a list box of draggable list items for
 * testing and buttons for exercising other capabilities of the info panel.
 */
public class InfoPanelTestController extends FrameworkController {
    
    private static final String TEST_EVENT = "INFOPANELTEST";
    
    private Listbox actions;
    
    private Listbox listbox;
    
    private Button btnDisable;
    
    private Checkbox chkEvents;
    
    private Checkbox chkAssociate;
    
    private Import include;
    
    private final InfoPanelTestDropRenderer dropRenderer = new InfoPanelTestDropRenderer();
    
    private IInfoPanel infoPanel;
    
    private boolean rendererDisabled;
    
    private int itemCount;
    
    private int menuCount;
    
    private int alertCount;
    
    private IEventManager eventManager;
    
    /**
     * Populate list box with some draggable items.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        
        while (itemCount < 11) {
            Listitem item = newListitem(itemCount % 2 == 1);
            item.addEventForward(DblclickEvent.TYPE, listbox, null);
            listbox.addChild(item);
        }
        
        infoPanel = (IInfoPanel) getController(include.findByName("infoPanelRoot"));
        eventManager = EventManager.getInstance();
    }
    
    /**
     * Creates and returns a draggable list item.
     * 
     * @param associateEvents If true, associate events with this item.
     * @return New list item.
     */
    private Listitem newListitem(boolean associateEvents) {
        Listitem item = new Listitem();
        
        if (associateEvents) {
            associateEvents(item);
        }
        
        String label = "Drop item #" + ++itemCount + (associateEvents ? " *" : "");
        item.setLabel(label);
        // Set to info panel drop id.
        item.setDragid(IInfoPanel.DROP_ID);
        // Create and attach the object to be rendered.
        DroppedItem dropped = new DroppedItem(label, "This is the detail for drop item #" + itemCount);
        item.setData(dropped);
        // Associate the drop renderer with the item.
        DropUtil.setDropRenderer(item, dropRenderer);
        return item;
    }
    
    /**
     * Creates and returns a test alert item.
     * 
     * @param associateEvents If true, associate events with this item.
     * @return Test alert item.
     */
    private BaseComponent newAlertitem(boolean associateEvents) {
        Div root = new Div(); // was vbox
        
        if (associateEvents) {
            associateEvents(root);
        }
        
        root.addChild(new Label("This is test alert #" + ++alertCount + (associateEvents ? " *" : "") + "."));
        
        for (int i = 0; i < 10; i++) {
            root.addChild(new Label("This is line #" + i));
        }
        
        return root;
    }
    
    /**
     * Associate action events with a component.
     * 
     * @param cmp The component.
     */
    private void associateEvents(BaseComponent cmp) {
        for (Action action : Action.values()) {
            InfoPanelService.associateEvent(cmp, getEvent(action), action);
        }
    }
    
    private String getEvent(Action action) {
        return TEST_EVENT + "." + action;
    }
    
    /**
     * Adds a new menu item to the info panel menu.
     */
    public void onClick$btnAddMenu() {
        Menuitem menuitem = new Menuitem();
        menuitem.setLabel(++menuCount + ". Click Me!");
        menuitem.addEventListener(ClickEvent.TYPE, (event) -> {
            System.out.println("Menu was clicked!");
        });
        infoPanel.registerMenuItem(menuitem, "Test");
    }
    
    /**
     * Toggle enable/disable renderer. Drop should be ignored when renderer is disabled.
     */
    public void onClick$btnDisable() {
        dropRenderer.setEnabled(rendererDisabled);
        rendererDisabled = !rendererDisabled;
        btnDisable.setLabel(rendererDisabled ? "Enable Drop" : "Disable Drop");
    }
    
    public void onClick$btnSendEvent() {
        Listitem item = actions.getSelectedItem();
        
        if (item != null) {
            Action action = Action.valueOf(item.getLabel());
            EventManager.getInstance().fireLocalEvent(getEvent(action), action);
        }
    }
    
    /**
     * Double-clicking a list item adds it to the info panel.
     */
    public void onDoubleClick$listbox() {
        pushDrop(listbox.getSelectedItem());
    }
    
    /**
     * Push a drop item to the info panel.
     */
    public void onClick$btnPushDrop() {
        pushDrop(newListitem(chkAssociate.isChecked()));
        
    }
    
    /**
     * Pushes the item to the info panel.
     * 
     * @param item The list item.
     */
    private void pushDrop(Listitem item) {
        if (item != null) {
            if (chkEvents.isChecked()) {
                eventManager.fireLocalEvent(IInfoPanel.DROP_EVENT_NAME, item);
            } else {
                infoPanel.drop(item);
            }
        }
    }
    
    /**
     * Test alert.
     */
    public void onClick$btnPushAlert() {
        pushAlert(newAlertitem(chkAssociate.isChecked()));
    }
    
    /**
     * Pushes the alert to the alert manager.
     * 
     * @param root The root component.
     */
    private void pushAlert(BaseComponent root) {
        if (chkEvents.isChecked()) {
            eventManager.fireLocalEvent(IInfoPanel.ALERT_EVENT_NAME, root);
        } else {
            infoPanel.showAlert(root);
        }
    }
    
    /**
     * Clear alerts.
     */
    public void onClick$btnClearAlerts() {
        infoPanel.clearAlerts();
    }
    
}
