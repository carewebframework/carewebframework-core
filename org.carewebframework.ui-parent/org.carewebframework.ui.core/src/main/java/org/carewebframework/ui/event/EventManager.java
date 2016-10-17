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
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;

/**
 * Subclasses framework's event manager to ensure that events are delivered in page's event thread
 * and to support delivering events sent from the client.
 */
public class EventManager extends org.carewebframework.api.event.EventManager {
    
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
            EventUtil.post(new Event(eventName, null, eventData));
        }
    }
    
    public Page getPage() {
        return page;
    }
    
    public void setPage(Page page) {
        this.page = page;
        page.registerEventListener(this);
    }
    
}
