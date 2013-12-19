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

import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.DesktopInit;
import org.zkoss.zk.ui.util.SessionCleanup;
import org.zkoss.zk.ui.util.SessionInit;
import org.zkoss.zk.ui.util.UiLifeCycle;

/**
 * Dispatches desktop, session, and component lifecycle events to registered listeners. This class
 * must be registered as a listener in the zk.xml configuration file as follows:
 * 
 * <pre>
 *  &lt;listener>
 *      &lt;listener-class>org.carewebframework.ui.LifecycleEventDispatcher&lt;/listener-class>
 *  &lt;/listener>
 * </pre>
 */
public class LifecycleEventDispatcher implements DesktopInit, DesktopCleanup, SessionInit, SessionCleanup, UiLifeCycle {
    
    private static final String ATTR_COMP_LISTENER = "@component_listener";
    
    private static final LifecycleEventListener<Desktop> desktopListener = new LifecycleEventListener<Desktop>();
    
    private static final LifecycleEventListener<Session> sessionListener = new LifecycleEventListener<Session>();
    
    /**
     * Add a desktop lifecycle callback.
     * 
     * @param callback
     */
    public static void addDesktopCallback(ILifecycleCallback<Desktop> callback) {
        desktopListener.addCallback(callback);
    }
    
    /**
     * Remove a desktop lifecycle callback.
     * 
     * @param callback
     */
    public static void removeDesktopCallback(ILifecycleCallback<Desktop> callback) {
        desktopListener.removeCallback(callback);
    }
    
    /**
     * Add a session lifecycle callback.
     * 
     * @param callback
     */
    public static void addSessionCallback(ILifecycleCallback<Session> callback) {
        sessionListener.addCallback(callback);
    }
    
    /**
     * Remove a session lifecycle callback.
     * 
     * @param callback
     */
    public static void removeSessionCallback(ILifecycleCallback<Session> callback) {
        sessionListener.removeCallback(callback);
    }
    
    /**
     * Add a component lifecycle callback.
     * 
     * @param comp Component whose lifecycle is to be monitored.
     * @param callback
     */
    public static void addComponentCallback(Component comp, ILifecycleCallback<Component> callback) {
        getListener(comp, true).addCallback(callback);
    }
    
    /**
     * Remove a component lifecycle callback.
     * 
     * @param comp Component whose lifecycle is to be monitored.
     * @param callback
     */
    public static void removeComponentCallback(Component comp, ILifecycleCallback<Component> callback) {
        LifecycleEventListener<Component> listener = getListener(comp, false);
        
        if (listener != null) {
            listener.removeCallback(callback);
            
            if (listener.isEmpty()) {
                comp.removeAttribute(ATTR_COMP_LISTENER);
            }
        }
    }
    
    /**
     * Returns the lifecycle event listener associated with the specified component.
     * 
     * @param comp The component whose event listener is sought.
     * @param autoCreate If true and no associated listener exists, one is created.
     * @return The associated event listener, or null if none exists and autoCreate is false.
     */
    private static LifecycleEventListener<Component> getListener(Component comp, boolean autoCreate) {
        @SuppressWarnings("unchecked")
        LifecycleEventListener<Component> listener = (LifecycleEventListener<Component>) comp
                .getAttribute(ATTR_COMP_LISTENER);
        
        if (listener == null && autoCreate) {
            listener = new LifecycleEventListener<Component>();
            comp.setAttribute(ATTR_COMP_LISTENER, listener);
        }
        
        return listener;
    }
    
    /**
     * Called by ZK listener dispatcher when a new desktop is initialized.
     * 
     * @see org.zkoss.zk.ui.util.DesktopInit#init(org.zkoss.zk.ui.Desktop, java.lang.Object)
     * @param desktop The desktop being initialized.
     * @param request The active request object.
     */
    @Override
    public void init(final Desktop desktop, final Object request) throws Exception {
        desktopListener.executeCallbacks(desktop, true);
    }
    
    /**
     * Called by ZK listener dispatcher when a desktop is about to be destroyed.
     * 
     * @see org.zkoss.zk.ui.util.DesktopCleanup#cleanup(org.zkoss.zk.ui.Desktop)
     * @param desktop The desktop being destroyed.
     */
    @Override
    public void cleanup(final Desktop desktop) throws Exception {
        desktopListener.executeCallbacks(desktop, false);
    }
    
    /**
     * Called by ZK listener dispatcher when a new session is initialized.
     * 
     * @see org.zkoss.zk.ui.util.SessionInit#init(org.zkoss.zk.ui.Session, java.lang.Object)
     * @param session The session being initialized.
     * @param request The active request object.
     */
    @Override
    public void init(Session session, Object request) throws Exception {
        sessionListener.executeCallbacks(session, true);
    }
    
    /**
     * Called by ZK listener dispatcher when a new session is about to be destroyed.
     * 
     * @see org.zkoss.zk.ui.util.SessionCleanup#cleanup(org.zkoss.zk.ui.Session)
     * @param session The session being destroyed.
     */
    @Override
    public void cleanup(Session session) throws Exception {
        sessionListener.executeCallbacks(session, false);
    }
    
    /**
     * Called by ZK listener dispatcher when a component is attached.
     * 
     * @see org.zkoss.zk.ui.util.UiLifeCycle#afterComponentAttached(Component, Page)
     * @param comp The component being attached.
     * @param page The page receiving the component.
     */
    @Override
    public void afterComponentAttached(Component comp, Page page) {
        executeCallbacks(comp, true);
    }
    
    /**
     * Called by ZK listener dispatcher when a component is detached.
     * 
     * @see org.zkoss.zk.ui.util.UiLifeCycle#afterComponentDetached(Component, Page)
     * @param comp The component being detached.
     * @param prevpage The page containing the component.
     */
    @Override
    public void afterComponentDetached(Component comp, Page prevpage) {
        executeCallbacks(comp, false);
    }
    
    /**
     * Execute callbacks for the specified component and its descendants.
     * 
     * @param comp Component whose callbacks are to be executed.
     * @param init If true, execute the onInit callback; otherwise, the onCleanup callback.
     */
    private void executeCallbacks(Component comp, boolean init) {
        LifecycleEventListener<Component> listener = getListener(comp, false);
        
        if (listener != null) {
            listener.executeCallbacks(comp, init);
        }
        
        for (Component child : comp.getChildren()) {
            executeCallbacks(child, init);
        }
    }
    
    @Override
    public void afterComponentMoved(Component parent, Component child, Component prevparent) {
        // ignored
    }
    
    @Override
    public void afterPageAttached(Page page, Desktop desktop) {
        // ignored
    }
    
    @Override
    public void afterPageDetached(Page page, Desktop prevdesktop) {
        // ignored
    }
}
