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
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.CompositeException;

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
        
        int getPriority();
        
    }
    
    private static final Log log = LogFactory.getLog(LifecycleEventListener.class);
    
    private static final Comparator<ILifecycleCallback<?>> comparator = new Comparator<ILifecycleCallback<?>>() {
        
        @Override
        public int compare(ILifecycleCallback<?> o1, ILifecycleCallback<?> o2) {
            return o1.getPriority() - o2.getPriority();
        }
        
    };
    
    private final List<ILifecycleCallback<T>> callbacks = Collections
            .synchronizedList(new ArrayList<ILifecycleCallback<T>>());
    
    private boolean needsSorting;
    
    public void addCallback(ILifecycleCallback<T> callback) {
        callbacks.add(callback);
        needsSorting = true;
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
        
        List<ILifecycleCallback<T>> list;
        
        synchronized (callbacks) {
            if (needsSorting) {
                needsSorting = false;
                Collections.sort(callbacks, comparator);
            }
            
            list = new ArrayList<ILifecycleCallback<T>>(callbacks);
        }
        
        int last = list.size() - 1;
        CompositeException exc = new CompositeException("Error executing lifecycle callback.");
        
        for (int i = 0; i <= last; i++) {
            ILifecycleCallback<T> callback = list.get(init ? i : last - i);
            
            try {
                if (init) {
                    callback.onInit(target);
                } else {
                    callback.onCleanup(target);
                }
            } catch (Exception e) {
                log.error("Error executing lifecycle callback.", e);
                exc.add(e);
            }
        }
        
        exc.throwIfExceptions();
    }
}
