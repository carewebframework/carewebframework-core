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

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.spring.SpringUtil;

import org.springframework.context.ApplicationContext;

import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;

/**
 * Resolve named references to container-managed variables. This is automatically registered to the
 * ZK page during desktop initialization.
 */
public class FrameworkVariableResolver implements VariableResolver {
    
    private ApplicationContext appContext;
    
    /**
     * Get the application context.
     * 
     * @return Application context.
     */
    private ApplicationContext getAppContext() {
        if (appContext == null && SpringUtil.isLoaded()) {
            appContext = SpringUtil.getAppContext();
        }
        
        return appContext;
    }
    
    /**
     * Resolve named variable by first searching the framework attribution map. If not there,
     * request it from the container.
     * 
     * @see org.zkoss.xel.VariableResolver#resolveVariable(java.lang.String)
     * @param name Name of the object to request.
     */
    @Override
    public Object resolveVariable(String name) throws XelException {
        if (getAppContext() == null) {
            return null;
        }
        
        Object object = null;
        
        if (FrameworkUtil.isInitialized()) {
            object = FrameworkUtil.getAttribute(name);
        }
        
        if (object == null && getAppContext().containsBean(name)) {
            object = getAppContext().getBean(name);
        }
        
        return object;
    }
    
}
