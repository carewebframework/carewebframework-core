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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Tracks timing information for a specific event.
 */
public class EventInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String eventName;
    
    private final String tag;
    
    private long start;
    
    /** Default to -1 to determine if zero is an actual elapsed time */
    private long elapsed = -1L;
    
    private RequestTime rt;
    
    private final String desktopId;
    
    private final String requestId;
    
    private String target;
    
    /**
     * Create an EventInfo instance for the specified event and tag.
     * 
     * @param event The event to be timed. If the event's data object is a RequestTime enum (which
     *            is the case for an onTimeEcho event), this specifies which time component is to be
     *            logged. If no RequestTime is specified, the time spent by the associated event
     *            listener is logged.
     * @param tag tag.
     * @param desktopId desktop id.
     * @param requestId request id.
     */
    public EventInfo(Event event, String tag, String desktopId, String requestId) {
        this.eventName = event.getName();
        this.tag = tag == null ? eventName : tag;
        this.desktopId = desktopId;
        this.requestId = requestId;
        Component target = event.getTarget();
        if (target != null) {
            this.target = target.toString();
        }
        start = System.currentTimeMillis();
        
        if (event.getData() instanceof RequestTime) {
            rt = (RequestTime) event.getData();
        }
    }
    
    /**
     * Called to signal the completion of the tracked event.
     */
    public void end() {
        elapsed = System.currentTimeMillis() - start;
    }
    
    /**
     * Returns the elapsed time.
     * 
     * @return the elapsed time.
     */
    public long getElapsed() {
        return elapsed;
    }
    
    /**
     * Returns the event name.
     * 
     * @return the event name.
     */
    public String getEventName() {
        return eventName;
    }
    
    /**
     * Returns the target.
     * 
     * @return the target.
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Prepare the event information for logging. If a RequestTime parameter was specified at
     * instantiation time, the start and elapsed times are computed from the performance data.
     * Otherwise, the start and elapsed times are based on the interval between instantiation and
     * the end signal for this object.
     * 
     * @param pd The performance data object tracking this event.
     */
    protected void prepare(PerformanceData pd) {
        if (rt != null) {
            start = pd.getStartTime(rt);
            elapsed = pd.getElapsedTime(rt);
            rt = null;
        }
    }
    
    /**
     * Render this object as a string in a format suitable for logging.
     */
    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("dtid", desktopId);
        map.put("reqid", requestId);
        map.put("target", target);
        map.put("event", eventName);
        return Util.formatForLogging(tag, start, elapsed, map);
    }
    
    /**
     * Tests for equality. Recognizes objects of type EventInfo and Event.
     * 
     * @param object object to compare.
     * @return equality.
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof EventInfo ? object == this : false;
    }
    
    /**
     * Returns the hash code of the event info.
     * 
     * @return the hash code of the event info.
     */
    @Override
    public int hashCode() {
        return eventName != null ? eventName.hashCode() : 0;
    }
}
