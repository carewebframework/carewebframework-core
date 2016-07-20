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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.spring.BeanRegistry;

/**
 * Tracks managed beans that implement the ThreadListenerRegistry.IThreadListener interface. These
 * beans will be notified when a new event thread or worker thread has been created or will be
 * destroyed.
 */
public class ThreadListenerRegistry extends BeanRegistry<String, ThreadListenerRegistry.IThreadListener> {
    
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
        for (IThreadListener subscriber : getInstance().getAll()) {
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
    
    @Override
    protected String getKey(IThreadListener item) {
        return getClass().getName();
    }
    
}
