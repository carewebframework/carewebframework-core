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
package org.carewebframework.ui.thread;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.thread.IAbortable;
import org.carewebframework.api.thread.ThreadUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;

/**
 * Used to run long operations in the background. Uses events to notify the requester of completion.
 * <p>
 * <i>Note: reference {@link #getException()} to evaluate any exception the ZKRunnable target may
 * have thrown. You may also invoke {@link #rethrow()}. One of these methods should be referenced to
 * ensure that the task successfully completed.</i>
 * </p>
 */
public class ThreadEx implements IAbortable {
    
    private static final Log log = LogFactory.getLog(ThreadEx.class);
    
    public static final String ON_THREAD_COMPLETE = "onThreadComplete";
    
    /**
     * Adds an abort method to the traditional Runnable interface. The target is responsible for
     * implementing this method so as to terminate its operation ASAP when this is called.
     */
    public interface IRunnable extends IAbortable {
        
        /**
         * Run method
         * 
         * @param thread The thread.
         * @throws Exception Unspecified exception.
         */
        void run(ThreadEx thread) throws Exception;
        
        /**
         * Abort the running thread.
         */
        @Override
        void abort();
    }
    
    /**
     * Subclasses the Thread class, notifying the requester when the target operation completes.
     */
    private class ThreadInternal extends Thread {
        
        /**
         * Executes the target operation with timings.
         */
        @Override
        public void run() {
            watch.start();
            
            try {
                target.run(ThreadEx.this);
            } catch (Throwable e) {
                exception = e;
            }
            watch.stop();
            ThreadEx.this.done();
        }
        
    }
    
    private boolean aborted;
    
    private Throwable exception;
    
    private final StopWatch watch = new StopWatch();
    
    private final IRunnable target;
    
    private final Event event;
    
    private final Page page;
    
    private final Thread thread;
    
    private final Map<String, Object> attribute = new HashMap<>();
    
    /**
     * Creates a thread for executing a background operation, using the default event name for
     * callback.
     * 
     * @param target Target operation.
     * @param requester ZK component requesting the operation.
     */
    public ThreadEx(IRunnable target, BaseComponent requester) {
        this(target, requester, ON_THREAD_COMPLETE);
    }
    
    /**
     * Creates a thread for executing a background operation.
     * 
     * @param target Target operation.
     * @param requester ZK component requesting the operation.
     * @param eventName Name of the event used to notify requester of completion. When fired, the
     *            data associated with the event will be a reference to this instance and may be
     *            interrogated to determine the outcome of the operation.
     */
    public ThreadEx(IRunnable target, BaseComponent requester, String eventName) {
        this.target = target;
        this.event = new Event(eventName, requester, this);
        this.page = requester.getPage();
        this.thread = new ThreadInternal();
    }
    
    /**
     * Starts the background thread.
     */
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("Executing ZKThread [target=" + target.getClass().getName() + "]");
        }
        ThreadUtil.startThread(this.thread);
    }
    
    /**
     * Request that the thread abort and notify the target.
     */
    @Override
    public void abort() {
        aborted = true;
        
        if (thread.isAlive()) {
            target.abort();
        }
    }
    
    /**
     * Returns true if the thread execution was aborted.
     * 
     * @return boolean true if aborted
     */
    public boolean isAborted() {
        return aborted;
    }
    
    /**
     * Invoked by the background thread when it has completed the target operation, even if aborted.
     * This schedules a notification on the desktop's event thread where the requester is notified
     * of the completion.
     */
    protected void done() {
        try {
            page.getEventQueue().queue(event);
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    /**
     * Returns the execution time in milliseconds of the thread.
     * 
     * @return long elapsed
     */
    public long getElapsed() {
        return watch.getTime();
    }
    
    /**
     * Returns the named attribute associated with this thread object.
     * 
     * @param name Name of the attribute.
     * @return Value of the attribute, or null if not found.
     */
    public Object getAttribute(String name) {
        synchronized (attribute) {
            return attribute.get(name);
        }
    }
    
    /**
     * Sets the named attribute to the specified value.
     * 
     * @param name Name of the attribute.
     * @param value Value to associate with the attribute.
     */
    public void setAttribute(String name, Object value) {
        synchronized (attribute) {
            attribute.put(name, value);
        }
    }
    
    /**
     * Returns the exception thrown by the background thread, or null if there was none.
     * 
     * @return Throwable
     */
    public Throwable getException() {
        return exception;
    }
    
    /**
     * Throws the saved exception in the current thread. If there is no saved exception, no action
     * is taken.
     * 
     * @throws Throwable when exception was thrown via ZKRunnable target
     */
    public void rethrow() throws Throwable {
        if (exception != null) {
            throw exception;
        }
    }
    
}
