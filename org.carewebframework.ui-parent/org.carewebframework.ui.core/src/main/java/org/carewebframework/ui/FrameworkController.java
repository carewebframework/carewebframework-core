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
package org.carewebframework.ui;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.api.thread.IAbortable;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;
import org.carewebframework.ui.thread.ThreadEx;
import org.carewebframework.ui.thread.ThreadEx.IRunnable;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;
import org.springframework.context.ApplicationContext;

/**
 * Can be subclassed to be used as a controller with convenience methods for accessing the
 * application context and framework services. The controller is automatically registered with the
 * framework and subclasses can implement special interfaces recognized by the framework, such as
 * context change interfaces.
 */
public class FrameworkController implements IAutoWired {
    
    private ApplicationContext appContext;
    
    private AppFramework appFramework;
    
    private IEventManager eventManager;
    
    protected BaseUIComponent root;
    
    private BaseUIComponent comp;
    
    private final List<IAbortable> threads = new ArrayList<>();
    
    private final IEventListener threadCompletionListener = new IEventListener() {
        
        /**
         * Background thread completion will be notified via this event listener. The listener will
         * in turn invoke either the threadFinished or threadAborted methods, as appropriate.
         * 
         * @param event The completion event.
         */
        @Override
        public void onEvent(Event event) {
            ThreadEx thread = (ThreadEx) event.getData();
            
            if (thread != null) {
                removeThread(thread);
                
                if (thread.isAborted()) {
                    threadAborted(thread);
                } else {
                    threadFinished(thread);
                }
            }
        }
        
    };
    
    private final IGenericEvent<Object> refreshListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            refresh();
        }
        
    };
    
    private final ILifecycleCallback<BaseComponent> lifecycleListener = new ILifecycleCallback<BaseComponent>() {
        
        @Override
        public void onInit(BaseComponent object) {
            eventManager.subscribe(Constants.REFRESH_EVENT, refreshListener);
            appFramework.registerObject(FrameworkController.this);
        }
        
        @Override
        public void onCleanup(BaseComponent object) {
            eventManager.unsubscribe(Constants.REFRESH_EVENT, refreshListener);
            appFramework.unregisterObject(FrameworkController.this);
            cleanup();
        }
        
        @Override
        public int getPriority() {
            return 0;
        }
        
    };
    
    /**
     * Returns the controller associated with the specified component, if any.
     * 
     * @param comp The component whose controller is sought.
     * @return The associated controller, or null if none found.
     */
    public static Object getController(BaseComponent comp) {
        return getController(comp, false);
    }
    
    /**
     * Returns the controller associated with the specified component, if any.
     * 
     * @param comp The component whose controller is sought.
     * @param recurse If true, search up the parent chain until a controller is found.
     * @return The associated controller, or null if none found.
     */
    public static Object getController(BaseComponent comp, boolean recurse) {
        return comp.findAttribute(Constants.ATTR_COMPOSER);
    }
    
    /**
     * Returns the application context associated with the active desktop.
     * 
     * @return An application context instance.
     */
    public ApplicationContext getAppContext() {
        return appContext;
    }
    
    /**
     * Returns the application framework associated with the active desktop.
     * 
     * @return An application framework instance.
     */
    public AppFramework getAppFramework() {
        return appFramework;
    }
    
    /**
     * Returns the event manager associated with the active desktop.
     * 
     * @return The event manager.
     */
    public IEventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Override the doAfterCompose method to set references to the application context and the
     * framework and register the controller with the framework.
     * 
     * @param comp BaseComponent associated with this controller.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        root = (BaseUIComponent) comp;
        this.comp = root;
        comp.setAttribute(Constants.ATTR_COMPOSER, this);
        comp.addEventListener(ThreadEx.ON_THREAD_COMPLETE, threadCompletionListener);
        appContext = SpringUtil.getAppContext();
        appFramework = FrameworkUtil.getAppFramework();
        eventManager = EventManager.getInstance();
    }
    
    /**
     * Process a refresh event.
     */
    public void onRefresh() {
        refresh();
    }
    
    /**
     * Override to respond to a refresh request.
     */
    public void refresh() {
        
    }
    
    /**
     * Override to perform any special cleanup.
     */
    public void cleanup() {
        
    }
    
    /**
     * Abort background thread if it is running.
     */
    protected void abortBackgroundThreads() {
        while (!threads.isEmpty()) {
            abortBackgroundThread(threads.get(0));
        }
    }
    
    /**
     * Abort background thread if it is running.
     * 
     * @param thread Thread to abort.
     */
    protected void abortBackgroundThread(IAbortable thread) {
        removeThread(thread).abort();
    }
    
    /**
     * Add a thread to the active list.
     * 
     * @param thread Thread to add.
     * @return The thread that was added.
     */
    protected IAbortable addThread(IAbortable thread) {
        threads.add(thread);
        return thread;
    }
    
    /**
     * Remove a thread from the active list.
     * 
     * @param thread Thread to remove.
     * @return The thread that was removed.
     */
    protected IAbortable removeThread(IAbortable thread) {
        threads.remove(thread);
        return thread;
    }
    
    /**
     * Returns true if any active threads are present.
     * 
     * @return True if any active threads are present.
     */
    protected boolean hasActiveThreads() {
        return !threads.isEmpty();
    }
    
    /**
     * Starts a background thread.
     * 
     * @param runnable The runnable to be executed in the background thread.
     * @return The new thread.
     */
    protected ThreadEx startBackgroundThread(IRunnable runnable) {
        ThreadEx thread = new ThreadEx(runnable, comp);
        addThread(thread);
        thread.start();
        return thread;
    }
    
    /**
     * Called when a background thread has completed.
     * 
     * @param thread The background thread.
     */
    protected void threadFinished(ThreadEx thread) {
        removeThread(thread);
    }
    
    /**
     * Called when a background thread has aborted.
     * 
     * @param thread The background thread.
     */
    protected void threadAborted(ThreadEx thread) {
        removeThread(thread);
    }
    
}
