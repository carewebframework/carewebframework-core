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
        
        try {
            if (FrameworkUtil.isInitialized()) {
                object = FrameworkUtil.getAttribute(name);
            }
            
            if (object == null && appContext.containsBean(name)) {
                object = appContext.getBean(name);
            }
        } catch (Exception e) {
            
        }
        
        return object;
    }
    
}
