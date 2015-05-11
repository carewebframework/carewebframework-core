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

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.event.AbstractGlobalEventDispatcher;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.Application;
import org.carewebframework.ui.Application.SessionInfo;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * This is a simple implementation of a global event dispatcher that delivers events to each active
 * desktop.
 */
public class GlobalEventDispatcher extends AbstractGlobalEventDispatcher {
    
    private Desktop desktop;
    
    private final EventListener<Event> eventListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            IEventManager eventManager = EventManager.getInstance();
            
            if (eventManager != null) {
                eventManager.fireLocalEvent(event.getName(), event.getData());
            }
        }
        
    };
    
    public Desktop getDesktop() {
        return desktop;
    }
    
    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
    }
    
    @Override
    public void subscribeRemoteEvent(String eventName, boolean subscribe) {
    }
    
    /**
     * Gets the unique id for this end point.
     * 
     * @return The end point's unique id.
     */
    @Override
    protected String getEndpointId() {
        return desktop.getId();
    }
    
    @Override
    public void fireRemoteEvent(String eventName, Serializable eventData, String recipients) {
        List<String> recp = StringUtils.isEmpty(recipients) ? null : StrUtil.toList(recipients, ",");
        
        for (SessionInfo sessionInfo : Application.getInstance().getActiveSessions()) {
            for (Desktop dktop : sessionInfo.getDesktops()) {
                if (recp == null || recp.contains(dktop.getId())) {
                    try {
                        Executions.schedule(dktop, eventListener, new Event(eventName, null, eventData));
                    } catch (Throwable t) {}
                    
                    if (recp != null) {
                        recp.remove(dktop.getId());
                        
                        if (recp.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
