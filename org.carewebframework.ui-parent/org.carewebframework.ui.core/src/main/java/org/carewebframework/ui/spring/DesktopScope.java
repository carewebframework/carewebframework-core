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
