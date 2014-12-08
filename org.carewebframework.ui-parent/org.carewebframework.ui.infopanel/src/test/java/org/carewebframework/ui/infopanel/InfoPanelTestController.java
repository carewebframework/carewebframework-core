/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.infopanel;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.infopanel.model.IInfoPanel;
import org.carewebframework.ui.infopanel.model.IInfoPanel.Action;
import org.carewebframework.ui.infopanel.service.InfoPanelService;
import org.carewebframework.ui.zk.DropUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Vbox;

/**
 * Controller for the test component. This component provides a list box of draggable list items for
 * testing and buttons for exercising other capabilities of the info panel.
 */
public class InfoPanelTestController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String TEST_EVENT = "INFOPANELTEST";
    
    private Listbox actions;
    
    private Listbox listbox;
    
    private Button btnDisable;
    
    private Checkbox chkEvents;
    
    private Checkbox chkAssociate;
    
    private Include include;
    
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
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        while (itemCount < 11) {
            Listitem item = newListitem(itemCount % 2 == 1);
            item.addForward(Events.ON_DOUBLE_CLICK, listbox, null);
            listbox.appendChild(item);
        }
        
        infoPanel = (IInfoPanel) getController(include.getFellow("infoPanelRoot"));
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
        item.setDraggable(IInfoPanel.DROP_ID);
        // Create and attach the object to be rendered.
        DroppedItem dropped = new DroppedItem(label, "This is the detail for drop item #" + itemCount);
        item.setValue(dropped);
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
    private Component newAlertitem(boolean associateEvents) {
        Vbox root = new Vbox();
        
        if (associateEvents) {
            associateEvents(root);
        }
        
        root.appendChild(new Label("This is test alert #" + ++alertCount + (associateEvents ? " *" : "") + "."));
        
        for (int i = 0; i < 10; i++) {
            root.appendChild(new Label("This is line #" + i));
        }
        
        return root;
    }
    
    /**
     * Associate action events with a component.
     * 
     * @param cmp The component.
     */
    private void associateEvents(Component cmp) {
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
        menuitem.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("Menu was clicked!");
            }
            
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
    private void pushAlert(Component root) {
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
