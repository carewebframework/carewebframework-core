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
package org.carewebframework.logging.perf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Performance monitor for timing requests and events. The monitor copies request timing data
 * (captured via the PerformanceMeter interface) into the current PerformanceData object associated
 * with the page making the request. The monitor must be registered as a listener in the zk.xml
 * configuration file as follows:
 * 
 * <pre>
 * {@literal
 * <listener>
 *      <description>Performance monitor</description>
 *      <listener-class>org.carewebframework.logging.perf4j.PerformanceMonitor</listener-class>
 * </listener>}
 * </pre>
 */
public class PerformanceMonitor implements PerformanceMeter, IPerformanceMonitorMXBean {
    
    private static final Log log = LogFactory.getLog(PerformanceMonitor.class);
    
    protected static final String EVENT_TIME_ECHO = "onTimeEcho";
    
    protected static final int MAX_PERFORMANCE_DATA_LIMIT = 1000;
    
    /** The limit must be defined before the map for the constructor to work properly. */
    private int performanceDataLimit = MAX_PERFORMANCE_DATA_LIMIT;
    
    private final Map<String, PerformanceData> performanceDataMap = new LRUMap<>();
    
    private boolean logRequestPerformance = true;
    
    private long threshold = 5000L;
    
    private boolean removePerformanceData = true;
    
    private final MBeanExporter mbeanExporter;
    
    private long expirationTimeMs = TimeUnit.MINUTES.toMillis(20);
    
    private final ScheduledExecutorService scheduledExecutorService;
    
    private ScheduledFuture<?> expirationTimerTaskFuture;
    
    /**
     * Constructs a PerformanceMonitor.
     * 
     * @throws Exception initialization exception.
     */
    public PerformanceMonitor() throws Exception {
        this(SpringUtil.getBean("mbeanExporter", MBeanExporter.class),
                SpringUtil.getBean("taskScheduler", ScheduledExecutorService.class));
    }
    
    /**
     * @param mbeanExporter The MBeanExporter used to register MBean
     * @param scheduledExecutorService The ScheduledExecutorService used to schedule expiration
     *            timer task
     */
    public PerformanceMonitor(MBeanExporter mbeanExporter, ScheduledExecutorService scheduledExecutorService) {
        this.mbeanExporter = mbeanExporter;
        this.scheduledExecutorService = scheduledExecutorService;
        startExpirationTimer();
        registerMBean();
    }
    
    private boolean isExpirationTimerTaskStopped() {
        return (this.expirationTimerTaskFuture == null) || this.expirationTimerTaskFuture.isCancelled();
    }
    
    /**
     * Starts the expiration timer.
     */
    @Override
    public synchronized void startExpirationTimer() {
        if (isExpirationTimerTaskStopped()) {
            this.expirationTimerTaskFuture = this.scheduledExecutorService.scheduleAtFixedRate(new ExpirationTimerTask(), 0L,
                1L, TimeUnit.MINUTES);
        } else {
            log.warn("PerformanceMonitorExpirationTimer already started");
        }
    }
    
    /**
     * Stops the expiration timer.
     */
    @Override
    public synchronized void stopExpirationTimer() {
        if (isExpirationTimerTaskStopped()) {
            log.warn("PerformanceMonitorExpirationTimer already stopped");
        } else {
            if (this.expirationTimerTaskFuture != null) {
                boolean isCancelled = this.expirationTimerTaskFuture.cancel(false);
                if (!isCancelled) {
                    log.warn("Unable to cancel ExpirationTimerTask Future");
                }
                this.expirationTimerTaskFuture = null;
            }
        }
    }
    
    protected void registerMBean() {
        if (this.mbeanExporter == null) {
            log.debug("Unable to register MBean");
        } else {
            this.mbeanExporter.registerManagedResource(this);
            log.info("PerformanceMonitor listener MBean registered");
        }
    }
    
    /**
     * Returns true if logging of request timing is enabled.
     * 
     * @return true if logging of request timing is enabled.
     */
    @Override
    public boolean isLogRequestPerformance() {
        return this.logRequestPerformance;
    }
    
    /**
     * Set to true to enable logging of request timing.
     * 
     * @param logRequestPerformance log request performance flag.
     */
    @Override
    public void setLogRequestPerformance(boolean logRequestPerformance) {
        this.logRequestPerformance = logRequestPerformance;
    }
    
    /**
     * Returns the logging threshold. Only log requests and events that take longer than this
     * amount.
     * 
     * @return the logging threshold.
     */
    @Override
    public long getThreshold() {
        return this.threshold;
    }
    
    /**
     * Sets the logging threshold.
     * 
     * @param threshold the logging threshold.
     */
    @Override
    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
    
    /**
     * Returns true if removing performance data after request completion is enabled.
     * 
     * @return true if removing performance data after request completion is enabled.
     */
    @Override
    public boolean isRemovePerformanceData() {
        return this.removePerformanceData;
    }
    
    /**
     * Set to true to enable removing performance data after request completion.
     * 
     * @param removePerformanceData the remove performance data after request completion flag to set
     */
    @Override
    public void setRemovePerformanceData(boolean removePerformanceData) {
        this.removePerformanceData = removePerformanceData;
    }
    
    /**
     * Returns the performance data limit.
     * 
     * @return the performance data limit.
     */
    @Override
    public int getPerformanceDataLimit() {
        return this.performanceDataLimit;
    }
    
    /**
     * Sets the performance data limit as long as it doesn't exceed the MAX_PERFORMANCE_DATA_LIMIT.
     * 
     * @param performanceDataLimit performance data limit.
     */
    @Override
    public void setPerformanceDataLimit(int performanceDataLimit) {
        if (performanceDataLimit <= MAX_PERFORMANCE_DATA_LIMIT) {
            this.performanceDataLimit = performanceDataLimit;
        }
    }
    
    /**
     * Returns the expiration time in minutes.
     * 
     * @return the expiration time in minutes.
     */
    @Override
    public long getExpirationTimeMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(this.expirationTimeMs);
    }
    
    /**
     * Sets the expiration time in minutes.
     * 
     * @param expirationTimeMinutes expiration time in minutes.
     */
    @Override
    public void setExpirationTimeMinutes(long expirationTimeMinutes) {
        this.expirationTimeMs = TimeUnit.MINUTES.toMillis(expirationTimeMinutes);
    }
    
    /**
     * Returns all the performance data.
     * 
     * @return all the performance data.
     */
    @Override
    public List<PerformanceData> getAllPerformanceData() {
        List<PerformanceData> pds = new ArrayList<>(this.performanceDataMap.values());
        Collections.sort(pds);
        return pds;
    }
    
    /**
     * Returns the completed performance data.
     * 
     * @return the completed performance data.
     */
    @Override
    public List<PerformanceData> getCompletedPerformanceData() {
        List<PerformanceData> pds = new ArrayList<>(this.performanceDataMap.size());
        for (PerformanceData pd : this.performanceDataMap.values()) {
            if (pd.isComplete()) {
                pds.add(pd);
            }
        }
        Collections.sort(pds);
        return pds;
    }
    
    /**
     * Clears the performance data.
     */
    @Override
    public void clearPerformanceData() {
        this.performanceDataMap.clear();
    }
    
    /**
     * Registers an event sent to the specified target for monitoring.
     * 
     * @param target The component that will be the target of the event.
     * @param eventName The name of the event to be monitored.
     * @param tag The tag to be included in the log entry.
     * @param displayElapsed If true, the performance information will be sent to the display.
     */
    public static void monitorEvent(BaseComponent target, String eventName, String tag, boolean displayElapsed) {
        Page dt = target.getPage();
        PerformanceData pd = (PerformanceData) dt.getAttribute(PerformanceData.ATTR_PERF_DATA);
        if (pd == null) {
            pd = new PerformanceData(dt);
            dt.setAttribute(PerformanceData.ATTR_PERF_DATA, pd);
        }
        pd.monitorEvent(target, eventName, tag, displayElapsed);
    }
    
    /**
     * Echos an event to the specified target for timing purposes. The round trip time will be
     * logged.
     * 
     * @param target The component that will be the target of the echoed event.
     * @param tag The tag to be included in the log entry.
     * @param displayElapsed If true, the performance information will be sent to the display.
     */
    public static void timeEcho(BaseComponent target, String tag, boolean displayElapsed) {
        timeEcho(target, tag, displayElapsed, RequestTime.TOTAL);
    }
    
    /**
     * Echos an event to the specified target for timing purposes.
     * 
     * @param target The component that will be the target of the echoed event.
     * @param tag The tag to be included in the log entry.
     * @param displayElapsed If true, the performance information will be sent to the display.
     * @param rt The time segment to be logged.
     */
    public static void timeEcho(BaseComponent target, String tag, boolean displayElapsed, RequestTime rt) {
        monitorEvent(target, EVENT_TIME_ECHO, tag, displayElapsed);
        Event event = new Event(EVENT_TIME_ECHO, target, rt);
        EventUtil.post(event);
    }
    
    /**
     * Updates the performance data for the page associated with the specified execution. Note that
     * a single performance data instance is associated with each unique page. When the unique
     * request id changes, the current performance data is logged and then reset to receive the data
     * for the new request id.
     * 
     * @param requestId The unique request id.
     * @param exec The current execution.
     * @param time The time being updated.
     * @param index The index of the time element being updated.
     */
    private void logTime(String requestId, ExecutionContext exec, long time, int index) {
        Page page = exec.getPage();
        String command = exec.get("cmd_0");
        if ("dummy".equals(command)) {
            return;
        }
        PerformanceData pd = this.performanceDataMap.get(requestId);
        if (pd == null) {
            pd = new PerformanceData(page);
            pd.setCommand(command);
            pd.setRequestId(requestId);
            this.performanceDataMap.put(requestId, pd);
            page.setAttribute(PerformanceData.ATTR_PERF_DATA, pd);
        }
        if (pd.isLogRequestPerformance() != this.logRequestPerformance) {
            pd.setLogRequestPerformance(this.logRequestPerformance);
        }
        if (pd.getThreshold() != this.threshold) {
            pd.setThreshold(this.threshold);
        }
        pd.setTime(index, time);
        // The request is complete at the client, so log the time
        if (index == 4) {
            boolean exceededThreshold = pd.logStatistics();
            if (this.removePerformanceData || !exceededThreshold) {
                this.performanceDataMap.remove(requestId);
            }
        }
    }
    
    // Interface: PerformanceMeter
    
    /**
     * @see org.zkoss.zk.ui.util.PerformanceMeter#requestStartAtClient(java.lang.String,
     *      org.zkoss.zk.ui.Execution, long)
     */
    @Override
    public void requestStartAtClient(String requestId, ExecutionContext exec, long time) {
        logTime(requestId, exec, time, 0);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.PerformanceMeter#requestStartAtServer(java.lang.String,
     *      org.zkoss.zk.ui.Execution, long)
     */
    @Override
    public void requestStartAtServer(String requestId, ExecutionContext exec, long time) {
        logTime(requestId, exec, time, 1);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.PerformanceMeter#requestCompleteAtServer(java.lang.String,
     *      org.zkoss.zk.ui.Execution, long)
     */
    @Override
    public void requestCompleteAtServer(String requestId, ExecutionContext exec, long time) {
        logTime(requestId, exec, time, 2);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.PerformanceMeter#requestReceiveAtClient(java.lang.String,
     *      org.zkoss.zk.ui.Execution, long)
     */
    @Override
    public void requestReceiveAtClient(String requestId, ExecutionContext exec, long time) {
        logTime(requestId, exec, time, 3);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.PerformanceMeter#requestCompleteAtClient(java.lang.String,
     *      org.zkoss.zk.ui.Execution, long)
     */
    @Override
    public void requestCompleteAtClient(String requestId, ExecutionContext exec, long time) {
        logTime(requestId, exec, time, 4);
    }
    
    /**
     * Least-recently used map implementation that limits the total entries in the map. WARNING:
     * This class is not thread-safe which should be fine because each request will be unique per
     * thread. However, setting the initial size to the performanceDataLimit and not allowing that
     * limit to be set above MAX_PERFORMANCE_DATA_LIMIT should prevent the race condition described
     * in:
     * <a href="https://tools.regenstrief.org/jira/browse/CWI-1459">https://tools.regenstrief.org
     * /jira/browse/CWI-1459</a>.
     * 
     * @param <K> Key class.
     * @param <V> Value class.
     */
    private class LRUMap<K, V> extends LinkedHashMap<K, V> {
        
        private static final long serialVersionUID = 1L;
        
        public LRUMap() {
            super(PerformanceMonitor.this.performanceDataLimit, 0.75f, true);
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean b = !PerformanceMonitor.this.removePerformanceData && (PerformanceMonitor.this.performanceDataLimit > 0)
                    && (PerformanceMonitor.this.performanceDataLimit < size());
            if (b && log.isTraceEnabled()) {
                log.trace("Evicting eldest entry: " + eldest.getKey());
            }
            return b;
        }
    }
    
    /**
     * TimerTask that expires entries in the LRUMap that are older than
     * <code>expirationTimeMs</code>.
     */
    private class ExpirationTimerTask extends TimerTask {
        
        /**
         * Expires any entries in the LRUMap that are older than <code>expirationTimeMs</code>.
         */
        @Override
        public void run() {
            try {
                for (Iterator<PerformanceData> iter = PerformanceMonitor.this.performanceDataMap.values().iterator(); iter
                        .hasNext();) {
                    PerformanceData pd = iter.next();
                    if (isExpired(pd)) {
                        log.warn(
                            String.format("Removing PerformanceData for request id %s after %d milliseconds of inactivity",
                                pd.getRequestId(), PerformanceMonitor.this.expirationTimeMs));
                        iter.remove();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        /**
         * Determines if a performance data entry is expired.
         * 
         * @param pd performance data.
         * @return true if expired, false otherwise.
         */
        private boolean isExpired(PerformanceData pd) {
            return pd.getStartTime(
                RequestTime.TOTAL) < (System.currentTimeMillis() - PerformanceMonitor.this.expirationTimeMs);
        }
    }
}
