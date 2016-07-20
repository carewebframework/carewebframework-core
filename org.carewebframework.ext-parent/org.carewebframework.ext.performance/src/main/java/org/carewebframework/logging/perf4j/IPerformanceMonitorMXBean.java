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

import java.util.List;

/**
 * This interface exposes PerformanceMonitor as a JMX <a
 * href="http://java.sun.com/docs/books/tutorial/jmx/mbeans/mxbeans.html">MX Bean</a>.
 */
public interface IPerformanceMonitorMXBean {
    
    /**
     * Returns true if logging of request timing is enabled.
     * 
     * @return true if logging of request timing is enabled.
     */
    public abstract boolean isLogRequestPerformance();
    
    /**
     * Set to true to enable logging of request timing.
     * 
     * @param logRequestPerformance log request performance flag.
     */
    public abstract void setLogRequestPerformance(boolean logRequestPerformance);
    
    /**
     * Returns the logging threshold. Only log requests and events that take longer than this
     * amount.
     * 
     * @return the logging threshold.
     */
    public abstract long getThreshold();
    
    /**
     * Sets the logging threshold.
     * 
     * @param threshold the logging threshold.
     */
    public abstract void setThreshold(long threshold);
    
    /**
     * Returns true if removing performance data after request completion is enabled.
     * 
     * @return true if removing performance data after request completion is enabled.
     */
    public abstract boolean isRemovePerformanceData();

    /**
     * Set to true to enable removing performance data after request completion.
     * 
     * @param removePerformanceData the remove performance data after request completion flag to set
     */
    public abstract void setRemovePerformanceData(boolean removePerformanceData);
    
    /**
     * Returns the performance data limit.
     * 
     * @return the performance data limit.
     */
    public abstract int getPerformanceDataLimit();
    
    /**
     * Sets the performance data limit.
     * 
     * @param performanceDataLimit performance data limit.
     */
    public abstract void setPerformanceDataLimit(int performanceDataLimit);
    
    /**
     * Returns the expiration time in minutes.
     * 
     * @return the expiration time in minutes.
     */
    public abstract long getExpirationTimeMinutes();
    
    /**
     * Sets the expiration time in minutes.
     * 
     * @param expirationTimeMinutes expiration time in minutes.
     */
    public abstract void setExpirationTimeMinutes(long expirationTimeMinutes);
    
    /**
     * Returns all the performance data.
     * 
     * @return all the performance data.
     */
    public abstract List<PerformanceData> getAllPerformanceData();
    
    /**
     * Returns the completed performance data.
     * 
     * @return the completed performance data.
     */
    public abstract List<PerformanceData> getCompletedPerformanceData();
    
    /**
     * Clears the performance data.
     */
    public abstract void clearPerformanceData();
    
    /**
     * Starts the expiration timer.
     */
    public abstract void startExpirationTimer();
    
    /**
     * Stops the expiration timer.
     */
    public abstract void stopExpirationTimer();
}
