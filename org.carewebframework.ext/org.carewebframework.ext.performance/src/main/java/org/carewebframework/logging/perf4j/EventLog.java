/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.logging.perf4j;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.zkoss.zk.ui.event.Event;

/**
 * Map of logged events for a single request. Since using the Event as a key causes memory leaks
 * (see https://tools.carewebframework.org/jira/browse/CWI-1235), we internally use the event's
 * toString() method as a key.
 */
public class EventLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String tag;
    
    private final Log log;
    
    private final String desktopId;
    
    private final String requestId;
    
    private EventInfo maxElapsedEventInfo;
    
    private final Map<String, EventInfo> events = new LinkedHashMap<String, EventInfo>();
    
    /**
     * Create an event log for the specified tag and logger.
     * 
     * @param tag tag.
     * @param logger logger.
     * @param desktopId desktop id.
     * @param requestId request id.
     */
    public EventLog(String tag, Log logger, String desktopId, String requestId) {
        super();
        this.tag = tag;
        this.log = logger;
        this.desktopId = desktopId;
        this.requestId = requestId;
    }
    
    /**
     * Returns the tag associated with this logger.
     * 
     * @return the tag associated with this logger.
     */
    public String getTag() {
        return tag;
    }
    
    /**
     * Returns the desktop id.
     * 
     * @return the desktop id.
     */
    public String getDesktopId() {
        return desktopId;
    }
    
    /**
     * Returns the request id.
     * 
     * @return the request id.
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Returns the max elapsed event info.
     * 
     * @return the max elapsed event info.
     */
    public EventInfo getMaxElapsedEventInfo() {
        return maxElapsedEventInfo;
    }
    
    /**
     * Returns the event info for the given key.
     * 
     * @param event event.
     * @return the event info for the given key.
     */
    public EventInfo get(Event event) {
        return events.get(event.toString());
    }
    
    /**
     * Maps an event to the corresponding event info.
     * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no
     *         mapping for <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public EventInfo put(Event key, EventInfo value) {
        EventInfo ei = events.put(key.toString(), value);
        if (maxElapsedEventInfo == null || maxElapsedEventInfo.getElapsed() < value.getElapsed()) {
            maxElapsedEventInfo = value;
        }
        return ei;
    }
    
    /**
     * Log all events.
     * 
     * @param pd The performance data instance whose events are being logged.
     */
    public void log(PerformanceData pd) {
        for (EventInfo eventInfo : events.values()) {
            eventInfo.prepare(pd);
            log.debug(eventInfo);
        }
    }
}
