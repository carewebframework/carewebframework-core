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
package org.carewebframework.api.event;

import java.util.List;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.api.messaging.PublisherInfo;

/**
 * Handles ping requests.
 */
public class PingEventHandler implements IGenericEvent<PingRequest> {
    
    public static final String EVENT_PING_REQUEST = "PING.REQUEST";
    
    private final IEventManager eventManager;
    
    private final IPublisherInfo publisherInfo;
    
    /**
     * Create the event handler.
     * 
     * @param eventManager The event manager.
     * @param publisherInfo Information about this publisher.
     */
    public PingEventHandler(IEventManager eventManager, IPublisherInfo publisherInfo) {
        super();
        this.eventManager = eventManager;
        this.publisherInfo = publisherInfo;
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    public void init() {
        eventManager.subscribe(EVENT_PING_REQUEST, this);
    }
    
    /**
     * Cleanup this instance.
     */
    public void destroy() {
        eventManager.unsubscribe(EVENT_PING_REQUEST, this);
    }
    
    @Override
    public void eventCallback(String eventName, PingRequest pingRequest) {
        if (checkFilters(pingRequest.filters)) {
            eventManager.fireRemoteEvent(pingRequest.responseEvent, new PublisherInfo(publisherInfo), pingRequest.requestor);
        }
    }
    
    private boolean checkFilters(List<PingFilter> filters) {
        if (filters != null) {
            for (PingFilter filter : filters) {
                switch (filter.type) {
                    case APP_NAME:
                        if (!filter.value.equals(publisherInfo.getAppName())) {
                            return false;
                        }
                        
                        break;
                    
                    case SENTINEL_EVENT:
                        if (!eventManager.hasSubscribers(filter.value)) {
                            return false;
                        }
                        
                        break;
                }
            }
        }
        
        return true;
    }
    
}
