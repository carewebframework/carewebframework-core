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

    private static ExecutorService taskExecutor;

    private static ScheduledExecutorService taskScheduler;

    /**
     * Returns the task scheduler, or null if one has not been defined. Instance of
     * ScheduledExecutorService.
     *
     * @return A scheduled executor service, or null if none defined.
     * @see TaskScheduler
     */
    public static ScheduledExecutorService getTaskScheduler() {
        return taskScheduler == null ? taskScheduler = SpringUtil.getBean("taskScheduler", ScheduledExecutorService.class)
                : taskScheduler;
    }

    /**
     * Returns the task executor, or null if one has not been defined. Instance of ExecutorService.
     *
     * @return An executor service, or null if none defined.
     * @see TaskExecutor
     */
    public static ExecutorService getTaskExecutor() {
        return taskExecutor == null ? taskExecutor = SpringUtil.getBean("taskExecutor", ExecutorService.class)
                : taskExecutor;
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
    }

}
