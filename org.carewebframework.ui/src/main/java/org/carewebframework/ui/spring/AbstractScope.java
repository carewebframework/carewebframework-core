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

import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.ui.util.RequestUtil;

/**
 * Abstract Spring scope for session-dependent scopes.
 * 
 * @param <T> The class of the target scope.
 */
public abstract class AbstractScope<T> extends org.carewebframework.api.spring.AbstractScope {
    
    private static final Log log = LogFactory.getLog(AbstractScope.class);
    
    private final ScopeContainer unscopedContainer;
    
    /**
     * Create the custom scope.
     * 
     * @param allowUnscoped If false, an attempt to access the scope outside a session context will
     *            fail. If true, an unscoped container is provided for access outside a session
     *            context.
     */
    protected AbstractScope(boolean allowUnscoped) {
        unscopedContainer = allowUnscoped ? new ScopeContainer() : null;
    }
    
    /**
     * Returns the container bound to the specified scope.
     * 
     * @param scope The target scope.
     * @return The bound container or null if none.
     */
    protected abstract ScopeContainer getScopeContainer(T scope);
    
    /**
     * Bind a container to the target scope.
     * 
     * @param scope The target scope.
     * @param container The container to be bound.
     */
    protected abstract void bindContainer(T scope, ScopeContainer container);
    
    /**
     * Returns the currently active scope.
     * 
     * @return The active scope, or null if cannot be determined.
     */
    protected abstract T getActiveScope();
    
    /**
     * Returns the container for the active scope.
     */
    @Override
    protected ScopeContainer getContainer() {
        return getContainer(null, true);
    }
    
    /**
     * Returns the key value used to bind the container to its scope.
     * 
     * @return Key value for this scope.
     */
    protected String getKey() {
        return getClass().getName();
    }
    
    /**
     * Returns the scope container for the scope. If a scope is not available, will use the session
     * scope initially, then transfer to the target scope when it becomes available.
     * 
     * @param scope Scope whose container is sought.
     * @param autoCreate If true, create a container if one is not found.
     * @return The container (possibly null).
     */
    protected ScopeContainer getContainer(T scope, boolean autoCreate) {
        ScopeContainer container = null;
        
        if (scope == null) {
            scope = getActiveScope();
        }
        
        if (scope != null) {
            container = getScopeContainer(scope, false);
            
            if (container == null) {
                HttpSession session = RequestUtil.getSession();
                container = getSessionContainer(session, false);
                
                if (container != null) {
                    if (container != unscopedContainer) {
                        session.removeAttribute(getKey());
                        bindContainer(scope, container);
                    }
                } else if (autoCreate) {
                    container = getScopeContainer(scope, true);
                }
                
            }
        } else {
            container = getSessionContainer(RequestUtil.getSession(), autoCreate);
        }
        
        return container;
    }
    
    /**
     * Returns the container associated with the specified scope.
     * 
     * @param scope The target scope.
     * @param autoCreate If true and a container does not exist, one will be created.
     * @return The requested container (may be null).
     */
    protected ScopeContainer getScopeContainer(T scope, boolean autoCreate) {
        ScopeContainer container = getScopeContainer(scope);
        
        if (container == null && autoCreate) {
            container = new ScopeContainer();
            bindContainer(scope, container);
        }
        
        return container;
    }
    
    /**
     * When the target scope is unavailable, the scope container is initially bound to the session.
     * When the target scope becomes available, the binding is transferred to that scope.
     * 
     * @param session The current session.
     * @param autoCreate If true, a scope container is created if it does not exist.
     * @return The scope container bound to the current session.
     */
    private ScopeContainer getSessionContainer(HttpSession session, boolean autoCreate) {
        if (session == null) {
            String message = "A session-dependent scope (" + getKey() + ") was referenced outside a session context.";
            
            if (unscopedContainer == null) {
                throw new IllegalStateException(message);
            }
            
            log.info(message);
            return unscopedContainer;
        }
        
        ScopeContainer container = (ScopeContainer) session.getAttribute(getKey());
        
        if (container == null && autoCreate) {
            container = new ScopeContainer();
            session.setAttribute(getKey(), container);
        }
        
        return container;
    }
}
