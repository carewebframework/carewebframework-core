/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.thread;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.thread.ThreadUtil;
import org.carewebframework.ui.FrameworkWebSupport;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

/**
 * Used to run long operations in the background. Uses ZK events to notify the requester of
 * completion.
 * <p>
 * <i>Note: reference {@link #getException()} to evaluate any exception the ZKRunnable target may
 * have thrown. You may also invoke {@link #rethrow()}. One of these methods should be referenced to
 * ensure that the task successfully completed.</i>
 * </p>
 */
public class ZKThread {
    
    private static final Log log = LogFactory.getLog(ZKThread.class);
    
    public static final String ON_THREAD_COMPLETE = "onThreadComplete";
    
    /**
     * Adds an abort method to the traditional Runnable interface. The target is responsible for
     * implementing this method so as to terminate its operation ASAP when this is called.
     */
    public interface ZKRunnable {
        
        /**
         * Run method
         * 
         * @param thread The thread.
         * @throws Exception Unspecified exception.
         */
        void run(ZKThread thread) throws Exception;
        
        /**
         * Auto generated method comment
         */
        void abort();
    }
    
    /**
     * Subclasses the Thread class, notifying the requester when the target operation completes.
     */
    private class ThreadEx extends Thread {
        
        /**
         * Executes the target operation with timings.
         */
        @Override
        public void run() {
            associateThread(desktop, requestAttributes);
            watch.start();
            
            try {
                target.run(ZKThread.this);
            } catch (final Throwable e) {
                exception = e;
            }
            watch.stop();
            disassociateThread();
            ZKThread.this.done();
        }
        
    }
    
    /**
     * The deferred event dispatcher receives the notification on the desktop's event thread that
     * the background thread has completed. It then posts an event (on the desktop's event thread)
     * to the requester that the operation has completed. The data associated with the event is a
     * reference to this ZKThread object and may be interrogated for the outcome of the operation.
     */
    private static final EventListener<Event> deferredEventDispatcher = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Events.postEvent(event);
        }
        
    };
    
    private boolean aborted;
    
    private Throwable exception;
    
    private final StopWatch watch = new StopWatch();
    
    private final ZKRunnable target;
    
    private final Event event;
    
    private final Desktop desktop;
    
    private final Thread thread;
    
    private final RequestAttributes requestAttributes;
    
    private final Map<String, Object> attribute = new HashMap<String, Object>();
    
    /**
     * Associates a desktop and request attributes with a background thread and notifies any thread
     * listeners.
     * 
     * @param desktop The desktop to associate.
     * @param requestAttributes The Spring request attributes to associate.
     */
    public static void associateThread(Desktop desktop, RequestAttributes requestAttributes) {
        RequestContextHolder.setRequestAttributes(requestAttributes, true);
        FrameworkWebSupport.associateDesktop(desktop);
        ThreadListenerRegistry.notifyListeners(true);
    }
    
    /**
     * Disassociates a desktop and request attributes from a background thread and notifies any
     * thread listeners.
     */
    public static void disassociateThread() {
        ThreadListenerRegistry.notifyListeners(false);
        FrameworkWebSupport.associateDesktop(null);
        RequestContextHolder.setRequestAttributes(null);
    }
    
    /**
     * Creates a thread for executing a background operation, using the default event name for
     * callback.
     * 
     * @param target Target operation.
     * @param requester ZK component requesting the operation.
     */
    public ZKThread(ZKRunnable target, Component requester) {
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
    public ZKThread(ZKRunnable target, Component requester, String eventName) {
        this.target = target;
        this.event = new Event(eventName, requester, this);
        this.desktop = requester.getDesktop();
        this.requestAttributes = RequestContextHolder.getRequestAttributes();
        this.thread = new ThreadEx();
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
            Executions.schedule(desktop, deferredEventDispatcher, event);
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
