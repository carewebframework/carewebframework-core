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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;

/**
 * One instance of this class is associated with each desktop request. The performance monitor
 * updates timing data dynamically during request processing.
 */
public class PerformanceData implements Serializable, Comparable<PerformanceData> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(PerformanceData.class);
    
    /**
     * Performance data attribute.
     */
    public static final String ATTR_PERF_DATA = PerformanceData.class.getName();
    
    private final long[] time = { 0, 0, 0, 0, 0 };
    
    private String requestId;
    
    private final String desktopId;
    
    private String command;
    
    private boolean logRequestPerformance = true;
    
    private byte complete;
    
    private final Map<String, EventLog> eventLogs = new HashMap<>();
    
    private long threshold;
    
    /**
     * Returns the logging threshold. Only log requests and events that take longer than this
     * amount.
     * 
     * @return the logging threshold.
     */
    public long getThreshold() {
        return threshold;
    }
    
    /**
     * Sets the logging threshold.
     * 
     * @param threshold the logging threshold.
     */
    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
    
    /**
     * Creates a new performance data instance associated with the specified desktop.
     * 
     * @param desktop desktop.
     */
    public PerformanceData(Desktop desktop) {
        desktopId = desktop.getId();
    }
    
    protected EventLog monitorEvent(Component target, String eventName, String tag, boolean displayElapsed) {
        return getEventLog(target, eventName, tag, true, displayElapsed);
    }
    
    /**
     * Returns the event log for the given event.
     * 
     * @param event event.
     * @return the event log for the given event.
     */
    public EventLog getEventLog(Event event) {
        return getEventLog(event.getTarget(), event.getName(), null, true, false);
    }
    
    private EventLog getEventLog(Component target, String eventName, String tag, boolean autoCreate, boolean displayElapsed) {
        if (target == null || target.getUuid() == null) {
            return null;
        }
        
        EventLog eventLog = eventLogs.get(requestId);
        
        if (eventLog == null && autoCreate) {
            eventLog = new EventLog(tag, log, desktopId, requestId);
            eventLogs.put(requestId, eventLog);
        }
        
        return eventLog;
    }
    
    /**
     * Returns the max elapsed event info.
     * 
     * @return the max elapsed event info.
     */
    private EventInfo getMaxElapsedEventInfo() {
        EventLog eventLog = eventLogs.get(requestId);
        return eventLog == null ? null : eventLog.getMaxElapsedEventInfo();
    }
    
    /**
     * Returns the max elapsed event info name.
     * 
     * @return the max elapsed event info name.
     */
    public String getMaxElapsedEventInfoName() {
        EventInfo eventInfo = getMaxElapsedEventInfo();
        return eventInfo != null ? eventInfo.getEventName() : null;
    }
    
    /**
     * Returns the max elapsed event info target.
     * 
     * @return the max elapsed event info target.
     */
    public String getMaxElapsedEventInfoTarget() {
        EventInfo eventInfo = getMaxElapsedEventInfo();
        return eventInfo != null ? eventInfo.getTarget() : null;
    }
    
    /**
     * Returns timing information for the specified index.
     * 
     * @param index index.
     * @return timing information for the specified index.
     */
    public long getTime(int index) {
        return time[index];
    }
    
    /**
     * Returns the start time for the specified interval.
     * 
     * @param requestTime The interval of interest.
     * @return the start time for the specified interval.
     */
    public long getStartTime(RequestTime requestTime) {
        switch (requestTime) {
            case SERVER:
                return time[1];
                
            case CLIENT:
                return time[3];
                
            case NETWORK:
                return time[0];
                
            case TOTAL:
                return time[0];
                
            default:
                return 0;
        }
    }
    
    /**
     * Returns the elapsed time for the specified interval.
     * 
     * @param requestTime The interval of interest.
     * @return the elapsed time for the specified interval.
     */
    public long getElapsedTime(RequestTime requestTime) {
        switch (requestTime) {
            case SERVER:
                return time[2] - time[1];
                
            case CLIENT:
                return time[4] - time[3];
                
            case NETWORK:
                return time[3] - time[0] - time[2] + time[1];
                
            case TOTAL:
                return time[4] - time[0];
                
            default:
                return 0;
        }
    }
    
    /**
     * Returns the server elapsed time.
     * 
     * @return the server elapsed time.
     */
    public long getServerElapsedTime() {
        return getElapsedTime(RequestTime.SERVER);
    }
    
    /**
     * Returns the client elapsed time.
     * 
     * @return the client elapsed time.
     */
    public long getClientElapsedTime() {
        return getElapsedTime(RequestTime.CLIENT);
    }
    
    /**
     * Returns the network elapsed time.
     * 
     * @return the network elapsed time.
     */
    public long getNetworkElapsedTime() {
        return getElapsedTime(RequestTime.NETWORK);
    }
    
    /**
     * Returns the total elapsed time.
     * 
     * @return the total elapsed time.
     */
    public long getTotalElapsedTime() {
        return getElapsedTime(RequestTime.TOTAL);
    }
    
    /**
     * Sets the time for the specified index.
     * 
     * @param index index.
     * @param value value.
     */
    public void setTime(int index, long value) {
        time[index] = value;
        if (value > 0) {
            complete |= (1 << index);
        }
    }
    
    /**
     * Resets performance data in preparation for a new request.
     */
    public void reset() {
        Arrays.fill(time, 0);
        complete = 0;
        command = null;
    }
    
    /**
     * Returns the unique id of the current request being timed.
     * 
     * @return the unique id of the current request being timed.
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Sets the id of the request being timed. If there are timing data present from a previous
     * request, these will be sent to the logger and the timing data reset.
     * 
     * @param requestId request id.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * Returns the command of the current request being timed.
     * 
     * @return the command of the current request being timed.
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * Sets the command of the request being timed.
     * 
     * @param command command.
     */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /**
     * Send timing data to the logger if the elapsed time exceeds the threshold.
     * 
     * @return whether the elapsed time exceeded the threshold.
     */
    public boolean logStatistics() {
        if (isComplete() && log.isDebugEnabled()) {
            long totalElapsedTime = getElapsedTime(RequestTime.TOTAL);
            if (totalElapsedTime > threshold) {
                logRequestStatistics();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Log timing data for any monitored events.
     */
    private void logEventStatistics() {
        EventLog eventLog = eventLogs.get(requestId);
        if (eventLog != null) {
            eventLog.log(this);
        }
    }
    
    /**
     * If request performance logging is enabled and the total elapsed time exceeds the threshold,
     * send timing data to the logger.
     */
    private void logRequestStatistics() {
        if (logRequestPerformance) {
            for (RequestTime rt : RequestTime.values()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("dtid", desktopId);
                map.put("reqid", requestId);
                map.put("cmd_0", command);
                log.debug(Util.formatForLogging("zkau." + rt.toString(), getStartTime(rt), getElapsedTime(rt), map));
            }
            logEventStatistics();
        }
    }
    
    /**
     * Returns true if logging of request timing is enabled.
     * 
     * @return true if logging of request timing is enabled.
     */
    public boolean isLogRequestPerformance() {
        return logRequestPerformance;
    }
    
    /**
     * Set to true to enable logging of request timing.
     * 
     * @param logRequestPerformance log request performance flag.
     */
    public void setLogRequestPerformance(boolean logRequestPerformance) {
        this.logRequestPerformance = logRequestPerformance;
    }
    
    /**
     * Returns true if timing is complete, i.e., all values are non-zero.
     * <p/>
     * This method is not public so it won't show up as a JMX CompositeData property.
     * 
     * @return true if timing is complete.
     */
    boolean isComplete() {
        return complete == ((1 << time.length) - 1);
    }
    
    /**
     * Returns the string value of the performance data.
     * 
     * @return the string value of the performance data.
     */
    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dtid", desktopId);
        map.put("reqid", requestId);
        map.put("cmd_0", command);
        EventInfo eventInfo = getMaxElapsedEventInfo();
        if (eventInfo != null) {
            map.put("maxElapasedEventInfo", eventInfo);
        }
        return Util.formatForLogging("Total PerformanceData", getStartTime(RequestTime.TOTAL), getTotalElapsedTime(), map);
    }
    
    /**
     * Sort the performance data by total elapsed time in descending order.
     */
    @Override
    public int compareTo(PerformanceData pd) {
        long elapsedTime1 = getTotalElapsedTime();
        long elapsedTime2 = pd != null ? pd.getTotalElapsedTime() : 0;
        return (elapsedTime1 > elapsedTime2) ? -1 : ((elapsedTime1 == elapsedTime2) ? 0 : 1);
    }
}
