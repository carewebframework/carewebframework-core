/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static utility class for the event operations.
 */
public class EventUtil {
    
    private static final Log log = LogFactory.getLog(EventUtil.class);
    
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
    public static void status(final String statusText) {
        try {
            getEventManager().fireLocalEvent(STATUS_EVENT, statusText == null ? "" : statusText);
        } catch (final Throwable e) {
            log.error(e);
        }
    }
    
    /**
     * Fires a ping request to specified or all recipients.
     * 
     * @param responseEvent Event to use for response.
     * @param filters Response filters (null for none).
     * @param recipients The recipient ids of the ping targets (or null for all recipients).
     */
    public static void ping(String responseEvent, List<PingFilter> filters, String recipients) {
        IEventManager eventManager = getEventManager();
        IGlobalEventDispatcher ged = ((ILocalEventDispatcher) eventManager).getGlobalEventDispatcher();
        
        if (ged != null) {
            PingRequest pingRequest = new PingRequest(responseEvent, filters, ged.getPublisherInfo().getEndpointId());
            eventManager.fireRemoteEvent(PingEventHandler.EVENT_PING_REQUEST, pingRequest, recipients);
        }
    }
    
    /**
     * Enforce static class.
     */
    private EventUtil() {
    };
    
}
