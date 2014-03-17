/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.spring;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.spring.AbstractScope;
import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.LifecycleEventDispatcher;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;
import org.carewebframework.ui.util.RequestUtil;

import org.zkoss.zk.ui.Desktop;

/**
 * Implements a custom Spring scope based on the ZK desktop.
 */
public class DesktopScope extends AbstractScope implements ILifecycleCallback<Desktop> {
    
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(DesktopScope.class);
    
    private static final String KEY_SCOPE = Desktop.class.getName() + ".container";
    
    public DesktopScope() {
        super();
        LifecycleEventDispatcher.addDesktopCallback(this);
    }
    
    private Desktop getDesktop() {
        return FrameworkWebSupport.getDesktop();
    }
    
    @Override
    protected ScopeContainer getContainer() {
        return getContainer(null, true);
    }
    
    /**
     * Returns the scope container for the desktop. If a desktop is not available, will use the
     * session scope initially, then transfer to the desktop scope.
     * 
     * @param desktop Desktop whose container is sought.
     * @param autoCreate If true, create a container if one is not found.
     * @return The container (possibly null).
     */
    private ScopeContainer getContainer(Desktop desktop, boolean autoCreate) {
        if (desktop == null) {
            desktop = getDesktop();
        }
        
        ScopeContainer container = null;
        
        if (desktop != null) {
            container = getDesktopContainer(desktop, false);
            
            if (container == null) {
                HttpSession session = RequestUtil.getSession();
                container = getSessionContainer(session, false);
                
                if (container != null) {
                    session.removeAttribute(KEY_SCOPE);
                    desktop.setAttribute(KEY_SCOPE, container);
                    container.setConversationId(desktop.getId());
                } else if (autoCreate) {
                    container = getDesktopContainer(desktop, true);
                }
                
            }
        } else {
            container = getSessionContainer(RequestUtil.getSession(), autoCreate);
        }
        
        return container;
    }
    
    private ScopeContainer getDesktopContainer(Desktop desktop, boolean autoCreate) {
        ScopeContainer container = (ScopeContainer) desktop.getAttribute(KEY_SCOPE);
        
        if (container == null && autoCreate) {
            container = new ScopeContainer();
            desktop.setAttribute(KEY_SCOPE, container);
            container.setConversationId(desktop.getId());
        }
        
        return container;
    }
    
    private ScopeContainer getSessionContainer(HttpSession session, boolean autoCreate) {
        if (session == null) {
            log.warn("Attempt to reference desktop scope outside a session context.");
            return autoCreate ? new ScopeContainer() : null;
        }
        
        ScopeContainer container = (ScopeContainer) session.getAttribute(KEY_SCOPE);
        
        if (container == null && autoCreate) {
            container = new ScopeContainer();
            session.setAttribute(KEY_SCOPE, container);
        }
        
        return container;
    }
    
    @Override
    public void onInit(Desktop desktop) {
        getContainer(desktop, false);
    }
    
    @Override
    public void onCleanup(Desktop desktop) {
        ScopeContainer container = getContainer(desktop, false);
        
        if (container != null) {
            container.destroy();
            desktop.removeAttribute(KEY_SCOPE);
        }
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}
