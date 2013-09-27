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
import java.util.List;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;
import org.carewebframework.ui.thread.ZKThread;
import org.carewebframework.ui.thread.ZKThread.ZKRunnable;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.context.ApplicationContext;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Can be subclassed to be used as a controller with convenience methods for accessing the
 * application context and framework services. The controller is automatically registered with the
 * framework and subclasses can implement special interfaces recognized by the framework, such as
 * context change interfaces.
 */
public class FrameworkController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private ApplicationContext appContext;
    
    private AppFramework appFramework;
    
    private IEventManager eventManager;
    
    protected Component root;
    
    private final List<ZKThread> threads = new ArrayList<ZKThread>();
    
    private final IGenericEvent<Object> refreshListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            refresh();
        }
        
    };
    
    private final ILifecycleCallback<Component> lifecycleListener = new ILifecycleCallback<Component>() {
        
        @Override
        public void onInit(Component object) {
        }
        
        @Override
        public void onCleanup(Component object) {
            eventManager.unsubscribe(Constants.REFRESH_EVENT, refreshListener);
            appFramework.unregisterObject(FrameworkController.this);
            cleanup();
        }
        
    };
    
    /**
     * Returns the controller associated with the specified component, if any.
     * 
     * @param comp The component whose controller is sought.
     * @return The associated controller, or null if none found.
     */
    public static Object getController(Component comp) {
        return comp.getAttribute(Constants.ATTR_COMPOSER);
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
     * @param comp Component associated with this controller.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
        comp.setAttribute(Constants.ATTR_COMPOSER, this);
        appContext = SpringUtil.getAppContext();
        appFramework = FrameworkUtil.getAppFramework();
        eventManager = EventManager.getInstance();
        appFramework.registerObject(this);
        LifecycleEventDispatcher.addComponentCallback(comp, lifecycleListener);
        eventManager.subscribe(Constants.REFRESH_EVENT, refreshListener);
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
    protected void abortBackgroundThread(ZKThread thread) {
        removeThread(thread).abort();
    }
    
    /**
     * Remove a thread from the active list. Clears the busy state if this was the last active
     * thread.
     * 
     * @param thread Thread to remove.
     * @return The thread that was removed.
     */
    protected ZKThread removeThread(ZKThread thread) {
        threads.remove(thread);
        return thread;
    }
    
    /**
     * Returns true if any active threads are present.
     * 
     * @return
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
    protected ZKThread startBackgroundThread(ZKRunnable runnable) {
        ZKThread thread = new ZKThread(runnable, root, "onThreadComplete");
        threads.add(thread);
        thread.start();
        return thread;
    }
    
    /**
     * Background thread completion will be notified via this event listener. The listener will in
     * turn invoke either the threadFinished or threadAborted methods, as appropriate.
     * 
     * @param event
     */
    public void onThreadComplete(Event event) {
        final ZKThread thread = (ZKThread) ZKUtil.getEventOrigin(event).getData();
        
        if (thread != null) {
            if (removeThread(thread).isAborted()) {
                threadAborted(thread);
            } else {
                threadFinished(thread);
            }
        }
    }
    
    /**
     * Called when a background thread has completed.
     * 
     * @param thread The background thread.
     */
    protected void threadFinished(ZKThread thread) {
    }
    
    /**
     * Called when a background thread has aborted.
     * 
     * @param thread The background thread.
     */
    protected void threadAborted(ZKThread thread) {
        
    }
    
}
