/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.popupsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.ui.zk.MoveEventListener;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 * Displays a popup message when a POPUP generic event is received.
 */
public class PopupSupport implements IGenericEvent<Object>, EventListener<Event> {
    
    private static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(PopupSupport.class);
    
    private static final String EVENT_POPUP = "POPUP";
    
    private static final int INITIAL_POSITION = 45;
    
    private PageDefinition popupDefinition = null;
    
    private int position = INITIAL_POSITION;
    
    private final List<Window> windows = Collections.synchronizedList(new ArrayList<Window>());
    
    private EventManager eventManager;
    
    /**
     * Subscribe to the popup event upon creation.
     */
    public PopupSupport() {
        super();
    }
    
    /**
     * Called after all properties have been injected by IOC container.
     */
    public void init() {
        eventManager.subscribe(EVENT_POPUP, this);
    }
    
    /**
     * Unsubscribe from popup event upon destruction.
     */
    public void destroy() {
        closeAll();
        eventManager.unsubscribe(EVENT_POPUP, this);
    }
    
    /**
     * Close all open popups.
     */
    public synchronized void closeAll() {
        for (Window window : windows) {
            try {
                window.removeEventListener(Events.ON_CLOSE, this);
                window.detach();
            } catch (Throwable e) {
                
            }
        }
        
        windows.clear();
        resetPosition();
    }
    
    /**
     * Popup event handler - display popup dialog upon receipt.
     * 
     * @param eventName Name of popupEvent
     * @param eventData May either be an encoded string (for backward compatibility) or a PopupData
     *            instance.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void eventCallback(String eventName, Object eventData) {
        try {
            PopupData popupData = null;
            
            if (eventData instanceof PopupData) {
                popupData = (PopupData) eventData;
            } else {
                popupData = new PopupData(eventData.toString());
            }
            
            if (popupData.isEmpty()) {
                return;
            }
            
            Page currentPage = ExecutionsCtrl.getCurrentCtrl().getCurrentPage();
            Window window = getPopupWindow();
            window.setTitle(popupData.getTitle());
            window.setPage(currentPage);
            window.addEventListener(Events.ON_MOVE, new MoveEventListener());
            String pos = getPosition();
            window.setLeft(pos);
            window.setTop(pos);
            window.addEventListener(Events.ON_CLOSE, this);
            Label label = (Label) window.getFellow("messagetext");
            label.setValue(popupData.getMessage());
            window.doOverlapped();
        } catch (Exception e) {}
    }
    
    /**
     * Reset window placement to initial position if no more popups are open.
     */
    private synchronized void resetPosition() {
        if (windows.size() == 0) {
            position = INITIAL_POSITION;
        }
    }
    
    /**
     * Return the screen position for window placement
     * 
     * @return The screen position.
     */
    private synchronized String getPosition() {
        position = position < 80 ? position + 5 : 10;
        return Integer.toString(position) + "%";
    }
    
    /**
     * Return a popup window instance.
     * 
     * @return A popup window instance.
     * @throws Exception Unspecified exception.
     */
    private synchronized Window getPopupWindow() throws Exception {
        if (popupDefinition == null) {
            popupDefinition = ZKUtil.loadZulPageDefinition(RESOURCE_PREFIX + "popupWindow.zul");
        }
        
        Window window = (Window) Executions.getCurrent().createComponents(popupDefinition, null, null);
        windows.add(window);
        return window;
    }
    
    @Override
    public void onEvent(Event event) throws Exception {
        windows.remove(event.getTarget());
        resetPosition();
    }
    
    /**
     * Set the event manager instance.
     * 
     * @param eventManager The event manager.
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Get the event manager instance.
     * 
     * @return The event manager.
     */
    public EventManager getEventManager() {
        return eventManager;
    }
}
