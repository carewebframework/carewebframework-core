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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.spring.BeanRegistry;

/**
 * Tracks managed beans that implement the ThreadListenerRegistry.IThreadListener interface. These
 * beans will be notified when a new event thread or worker thread has been created or will be
 * destroyed.
 */
public class ThreadListenerRegistry extends BeanRegistry<ThreadListenerRegistry.IThreadListener> {
    
    private static final ThreadListenerRegistry instance = new ThreadListenerRegistry();
    
    private static final Log log = LogFactory.getLog(ThreadListenerRegistry.class);
    
    /**
     * Notification interface for thread listeners.
     */
    public interface IThreadListener {
        
        /**
         * Called on thread initialization.
         */
        void onThreadInit();
        
        /**
         * Called on thread cleanup.
         */
        void onThreadCleanup();
    }
    
    public static ThreadListenerRegistry getInstance() {
        return instance;
    }
    
    /**
     * Notify listeners of thread activity.
     * 
     * @param init If true, is thread initiation; otherwise is thread termination.
     */
    public static void notifyListeners(boolean init) {
        for (IThreadListener subscriber : getInstance().getMembers()) {
            try {
                if (init) {
                    subscriber.onThreadInit();
                } else {
                    subscriber.onThreadCleanup();
                }
            } catch (Throwable t) {
                log.warn("Exception occurred during callback.", t);
            }
        }
    }
    
    /**
     * Enforce singleton instance.
     */
    private ThreadListenerRegistry() {
        super(IThreadListener.class);
    }
    
}
