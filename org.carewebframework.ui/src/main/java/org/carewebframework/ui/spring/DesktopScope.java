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

import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.LifecycleEventDispatcher;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;

import org.zkoss.zk.ui.Desktop;

/**
 * Implements a custom Spring scope based on the ZK desktop.
 */
public class DesktopScope extends AbstractScope<Desktop> implements ILifecycleCallback<Desktop> {
    
    public DesktopScope() {
        super(true);
        LifecycleEventDispatcher.addDesktopCallback(this);
    }
    
    @Override
    protected ScopeContainer getScopeContainer(Desktop scope) {
        return (ScopeContainer) scope.getAttribute(getKey());
    }
    
    @Override
    protected void bindContainer(Desktop scope, ScopeContainer container) {
        scope.setAttribute(getKey(), container);
        container.setConversationId(scope.getId());
    }
    
    @Override
    protected Desktop getActiveScope() {
        return FrameworkWebSupport.getDesktop();
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
            desktop.removeAttribute(getKey());
        }
    }
    
    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }
    
}
