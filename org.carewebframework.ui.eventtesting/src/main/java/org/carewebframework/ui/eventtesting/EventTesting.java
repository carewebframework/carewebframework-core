/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.eventtesting;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Plug-in to test remote events.
 */
public class EventTesting extends Window implements IGenericEvent<String> {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox eventName;
    
    private Textbox eventRecipients;
    
    private Textbox eventData;
    
    private Textbox eventResults;
    
    private Textbox newEvent;
    
    private Listbox eventList;
    
    private Checkbox autoGenerate;
    
    private Label info;
    
    private final IEventManager eventManager = EventManager.getInstance();
    
    private int messageCount;
    
    public void onCreate() {
        ZKUtil.wireController(this);
    }
    
    public void onClick$btnSend() {
        messageCount++;
        
        if (autoGenerate.isChecked()) {
            eventData.setText("Sending test event #" + messageCount);
        }
        
        eventManager.fireRemoteEvent(eventName.getText(), eventData.getText(), eventRecipients.getText());
        info("Fired", eventName.getText());
    }
    
    public void onClick$btnReset() {
        eventName.setText("");
        eventRecipients.setText("");
        eventData.setText("");
    }
    
    public void onClick$btnClear() {
        eventResults.setText("");
    }
    
    public void onClick$addEvent() {
        String eventName = newEvent.getText().trim();
        
        if (!StringUtils.isEmpty(eventName) && !containsEvent(eventName)) {
            Listitem item = new Listitem(eventName);
            eventList.appendChild(item);
        }
        
        newEvent.setText("");
    }
    
    private boolean containsEvent(String eventName) {
        for (Object object : eventList.getItems()) {
            if (((Listitem) object).getLabel().equals(eventName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void onSelect$eventList(Event event) {
        @SuppressWarnings("rawtypes")
        SelectEvent sel = (SelectEvent) ZKUtil.getEventOrigin(event);
        Listitem item = (Listitem) sel.getReference();
        String eventName = item.getLabel();
        
        if (item.isSelected()) {
            eventManager.subscribe(eventName, this);
            info("Subscribed to", eventName);
        } else {
            eventManager.unsubscribe(eventName, this);
            info("Unsubscribed from", eventName);
        }
    }
    
    private void info(String action, String eventName) {
        info.setValue(action + " '" + eventName + " ' event.");
    }
    
    @Override
    public void eventCallback(String eventName, String eventData) {
        String s = eventResults.getText();
        s += "\n\n" + eventName + ":\n" + eventData;
        eventResults.setText(s);
        //eventResults.smartUpdate("scrollTop", "1000000");
        info("Received", eventName);
    }
    
}
