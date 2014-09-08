/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.spring.SpringUtil;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

/**
 * Static utility class for the thread management.
 */
public class ThreadUtil {
    
    private static final Log log = LogFactory.getLog(ThreadUtil.class);
    
    /**
     * Bean ID for shared {@linkplain TaskExecutor}
     */
    protected static final String TASK_EXECUTOR_BEANID = "taskExecutor";
    
    /**
     * Bean ID for shared {@linkplain TaskScheduler}
     */
    protected static final String TASK_SCHEDULER_BEANID = "taskScheduler";
    
    /**
     * Returns the task scheduler, or null if one has not been defined. Instance of
     * ScheduledExecutorService.
     * 
     * @return A scheduled executor service, or null if none defined.
     * @see TaskScheduler
     */
    public static ScheduledExecutorService getTaskScheduler() {
        return SpringUtil.getBean(TASK_SCHEDULER_BEANID, ScheduledExecutorService.class);
    }
    
    /**
     * Returns the task executor, or null if one has not been defined. Instance of ExecutorService.
     * 
     * @return An executor service, or null if none defined.
     * @see TaskExecutor
     */
    public static ExecutorService getTaskExecutor() {
        return SpringUtil.getBean(TASK_EXECUTOR_BEANID, ExecutorService.class);
    }
    
    /**
     * Starts a thread. If an executor service is available, that is used. Otherwise, the thread's
     * start method is called.
     * 
     * @param thread Thread to start.
     */
    public static void startThread(Thread thread) {
        if (log.isDebugEnabled()) {
            log.debug("Starting background thread: " + thread);
        }
        
        ExecutorService executor = getTaskExecutor();
        
        if (executor != null) {
            executor.execute(thread);
        } else {
            thread.start();
        }
    }
    
    /**
     * Enforce static class.
     */
    private ThreadUtil() {
    };
    
}
