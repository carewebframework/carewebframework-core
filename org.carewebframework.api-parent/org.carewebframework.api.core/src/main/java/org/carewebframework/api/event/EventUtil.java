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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.messaging.Recipient;

/**
 * Static utility class for the event operations.
 */
public class EventUtil {
    
    private static final Log log = LogFactory.getLog(EventUtil.class);
    
    private static final String EVENT_PREFIX = "cwf-event-";

    public static final String STATUS_EVENT = "STATUS";
    
    /**
     * Returns the event manager for this application context.
     *
     * @return IEventManager
     */
    public static IEventManager getEventManager() {
        return EventManager.getInstance();
    }
    
    /**
     * Fires a generic event of type STATUS with no status text. Used to signal subscribers to clear
     * any status information.
     */
    public static void status() {
        status(null);
    }
    
    /**
     * Fires a generic event of type STATUS to update any object that subscribes to it.
     *
     * @param statusText Text associated with the status change.
     */
    public static void status(String statusText) {
        try {
            getEventManager().fireLocalEvent(STATUS_EVENT, statusText == null ? "" : statusText);
        } catch (Throwable e) {
            log.error(e);
        }
    }
    
    /**
     * Fires a ping request to specified or all recipients.
     *
     * @param responseEvent Event to use for response.
     * @param filters Response filters (null for none).
     * @param recipients The list of ping recipients (or none for all recipients).
     */
    public static void ping(String responseEvent, List<PingFilter> filters, Recipient... recipients) {
        IEventManager eventManager = getEventManager();
        IGlobalEventDispatcher ged = ((ILocalEventDispatcher) eventManager).getGlobalEventDispatcher();
        
        if (ged != null) {
            ged.Ping(responseEvent, filters, recipients);
        }
    }
    
    /**
     * Returns the messaging channel name from the event name.
     *
     * @param eventName The event name.
     * @return The channel name.
     */
    public static String getChannelName(String eventName) {
        return eventName == null ? null : EVENT_PREFIX + eventName.split("\\.", 2)[0];
    }
    
    /**
     * Returns the event name from the channel name.
     *
     * @param channelName The channel name.
     * @return The event name.
     */
    public static String getEventName(String channelName) {
        int i = channelName.indexOf(EVENT_PREFIX);
        return i < 0 ? channelName : channelName.substring(i + EVENT_PREFIX.length());
    }
    
    /**
     * Enforce static class.
     */
    private EventUtil() {
    }
    
}
