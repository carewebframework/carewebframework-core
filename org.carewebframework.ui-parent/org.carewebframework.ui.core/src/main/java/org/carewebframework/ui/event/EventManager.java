/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.event;

import org.carewebframework.ui.zk.ZKUtil;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * Subclasses framework's event manager to ensure that events are delivered in desktop's event
 * thread and to support delivering events sent from the client.
 */
public class EventManager extends org.carewebframework.api.event.EventManager implements AuService {
    
    private static final String GENERIC_EVENT = "onGenericEvent";
    
    private Desktop desktop;
    
    private final EventListener<Event> eventListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            EventManager.super.fireLocalEvent(event.getName(), event.getData());
        }
        
    };
    
    /**
     * Fires the event to local subscribers. Ensures that event delivery takes place in the
     * desktop's event thread.
     * 
     * @see org.carewebframework.api.event.EventManager#fireLocalEvent(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void fireLocalEvent(String eventName, Object eventData) {
        if (ZKUtil.inEventThread(desktop)) {
            super.fireLocalEvent(eventName, eventData);
        } else {
            Executions.schedule(desktop, eventListener, new Event(eventName, null, eventData));
        }
    }
    
    public Desktop getDesktop() {
        return desktop;
    }
    
    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
        desktop.addListener(this);
    }
    
    /**
     * Catch generic event requests from client and deliver them.
     */
    @Override
    public boolean service(AuRequest request, boolean everError) {
        String cmd = request.getCommand();
        return GENERIC_EVENT.equals(cmd) ? doEvent(request) : false;
    }
    
    /**
     * Deliver a local or remote event from client.
     * 
     * @param request The request object.
     * @return Always true.
     */
    private boolean doEvent(AuRequest request) {
        String eventName = request.getData().get("eventName").toString();
        Object eventData = request.getData().get("eventData");
        Boolean asLocal = (Boolean) request.getData().get("asLocal");
        
        if (asLocal) {
            fireLocalEvent(eventName, eventData);
        } else {
            fireRemoteEvent(eventName, eventData);
        }
        
        return true;
    }
}
