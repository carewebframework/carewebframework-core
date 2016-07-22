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
package org.carewebframework.ui.event;

import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

/**
 * Subclasses framework's event manager to ensure that events are delivered in page's event thread
 * and to support delivering events sent from the client.
 */
public class EventManager extends org.carewebframework.api.event.EventManager implements AuService {
    
    private static final String GENERIC_EVENT = "onGenericEvent";
    
    private Page page;
    
    private final IEventListener eventListener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            EventManager.super.fireLocalEvent(event.getType(), event.getData());
        }
        
    };
    
    /**
     * Fires the event to local subscribers. Ensures that event delivery takes place in the page's
     * event thread.
     * 
     * @see org.carewebframework.api.event.EventManager#fireLocalEvent(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void fireLocalEvent(String eventName, Object eventData) {
        if (ZKUtil.inEventThread(page)) {
            super.fireLocalEvent(eventName, eventData);
        } else {
            Executions.schedule(page, eventListener, new Event(eventName, null, eventData));
        }
    }
    
    public Page getPage() {
        return page;
    }
    
    public void setPage(Page page) {
        this.page = page;
        page.addListener(this);
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
