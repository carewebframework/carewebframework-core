/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dispatches creation and destruction events to registered listeners.
 * 
 * @param <T> The object whose creation and destruction is being monitored.
 */
public class LifecycleEventListener<T extends Object> {
    
    /**
     * Registered listeners must implement this interface.
     * 
     * @param <T>
     */
    public interface ILifecycleCallback<T> {
        
        void onInit(T object);
        
        void onCleanup(T object);
        
    }
    
    private static final Log log = LogFactory.getLog(LifecycleEventListener.class);
    
    private final List<ILifecycleCallback<T>> callbacks = Collections
            .synchronizedList(new ArrayList<ILifecycleCallback<T>>());
    
    public void addCallback(ILifecycleCallback<T> callback) {
        callbacks.add(callback);
    }
    
    public void removeCallback(ILifecycleCallback<T> callback) {
        callbacks.remove(callback);
    }
    
    public boolean isEmpty() {
        return callbacks.isEmpty();
    }
    
    /**
     * Executes all registered listeners.
     * 
     * @param target Target object.
     * @param init If true, this is an init event; otherwise, a cleanup event.
     */
    public void executeCallbacks(T target, boolean init) {
        if (log.isInfoEnabled()) {
            log.info("Executing lifecycle callbacks for " + target + (init ? " init." : " cleanup."));
        }
        
        List<ILifecycleCallback<T>> list = new ArrayList<ILifecycleCallback<T>>(callbacks);
        
        if (!init) {
            Collections.reverse(list);
        }
        
        for (ILifecycleCallback<T> callback : list) {
            try {
                if (init) {
                    callback.onInit(target);
                } else {
                    callback.onCleanup(target);
                }
            } catch (Exception e) {
                log.error("Error executing lifecycle callback.", e);
            }
        }
    }
}
