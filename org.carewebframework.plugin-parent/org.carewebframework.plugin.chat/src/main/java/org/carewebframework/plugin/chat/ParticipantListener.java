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
package org.carewebframework.plugin.chat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.event.PingFilter;
import org.carewebframework.api.event.PingFilter.PingFilterType;
import org.carewebframework.api.messaging.IPublisherInfo;

/**
 * Class for listening to participant changes.
 */
public class ParticipantListener {
    
    private static final AtomicInteger pingId = new AtomicInteger();
    
    /**
     * Call back interface for signaling participant changes.
     */
    public interface IParticipantUpdate {
        
        /**
         * Called when a participant has been added.
         * 
         * @param participant The participant that was added.
         * @param fromRefresh If true, the participant was added because of a refresh request.
         */
        void onParticipantAdded(IPublisherInfo participant, boolean fromRefresh);
        
        /**
         * Called when a participant is removed.
         * 
         * @param participant The participant that was removed.
         */
        void onParticipantRemoved(IPublisherInfo participant);
    }
    
    private enum EventSubType {
        PING, ADD, REMOVE
    };
    
    /**
     * Listener for a specific event subtype.
     */
    private class SubTypeListener implements IGenericEvent<IPublisherInfo> {
        
        private final EventSubType subtype;
        
        private final String eventName;
        
        private boolean active;
        
        SubTypeListener(EventSubType subtype, String eventName) {
            this.subtype = subtype;
            this.eventName = eventName;
        }
        
        @Override
        public void eventCallback(String eventName, IPublisherInfo participant) {
            switch (subtype) {
                case PING:
                    callback.onParticipantAdded(participant, true);
                    break;
                
                case ADD:
                    callback.onParticipantAdded(participant, false);
                    break;
                
                case REMOVE:
                    callback.onParticipantRemoved(participant);
                    break;
            }
        }
        
        public void setActive(boolean active) {
            if (active != this.active) {
                this.active = active;
                
                if (active) {
                    eventManager.subscribe(eventName, this);
                } else {
                    eventManager.unsubscribe(eventName, this);
                }
            }
        }
        
    }
    
    private final SubTypeListener refreshListener;
    
    private final SubTypeListener addListener;
    
    private final SubTypeListener removeListener;
    
    private final List<PingFilter> pingFilter;
    
    private final String pingEvent = ChatService.EVENT_PING + "." + pingId.incrementAndGet();
    
    private final IParticipantUpdate callback;
    
    protected final IEventManager eventManager;
    
    protected final IPublisherInfo self;
    
    /**
     * Creates a participant listener.
     * 
     * @param self The publisher info of the owner of this listener.
     * @param sentinelEvent The sentinel event that will be used to filter participants.
     * @param addEvent The event that signals a new participant has been added.
     * @param removeEvent The event that signals a participant has been removed.
     * @param eventManager The event manager instance.
     * @param callback The callback interface to invoke when a participant event has been received.
     */
    protected ParticipantListener(IPublisherInfo self, String sentinelEvent, String addEvent, String removeEvent,
        IEventManager eventManager, IParticipantUpdate callback) {
        this.self = self;
        this.eventManager = eventManager;
        this.callback = callback;
        refreshListener = new SubTypeListener(EventSubType.PING, pingEvent);
        addListener = new SubTypeListener(EventSubType.ADD, addEvent);
        removeListener = new SubTypeListener(EventSubType.REMOVE, removeEvent);
        pingFilter = Collections.singletonList(new PingFilter(PingFilterType.SENTINEL_EVENT, sentinelEvent));
    }
    
    /**
     * Sets the active state.
     * 
     * @param active The new active state. When set to true, all event subscriptions are activated
     *            and a participant add event is fired globally. When set to false, all event
     *            subscriptions are inactivated and a participant remove event is fired globally.
     */
    public void setActive(boolean active) {
        refreshListener.setActive(active);
        addListener.setActive(active);
        removeListener.setActive(active);
        eventManager.fireRemoteEvent(active ? addListener.eventName : removeListener.eventName, self);
        
        if (active) {
            refresh();
        }
    }
    
    /**
     * Sends a ping request to all candidate participants.
     */
    public void refresh() {
        EventUtil.ping(pingEvent, pingFilter);
    }
    
}
