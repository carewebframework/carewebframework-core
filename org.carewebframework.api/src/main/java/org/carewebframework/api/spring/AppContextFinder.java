/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.carewebframework.api.AppFramework;

import org.springframework.context.ApplicationContext;

/**
 * This is a simple default implementation for locating the application context. It assumes a single
 * global application context.
 */
public class AppContextFinder implements IAppContextFinder {
    
    private AppFramework appFramework;
    
    /**
     * Constructor registers this as the framework's application context finder.
     */
    public AppContextFinder() {
        SpringUtil.setAppContextFinder(this);
    }
    
    /**
     * Sets the application framework instance associated with the finder. This is typically
     * injected by the IOC container.
     * 
     * @param appFramework An application framework instance.
     */
    public void setAppFramework(AppFramework appFramework) {
        this.appFramework = appFramework;
    }
    
    /**
     * Returns the application framework instance associated with the finder.
     * 
     * @return The application framework instance.
     */
    public AppFramework getAppFramework() {
        return appFramework;
    }
    
    /**
     * <b>IAppContextFinder.getAppContext</b>: Returns the application context for the current
     * scope.
     */
    @Override
    public ApplicationContext getAppContext() {
        return appFramework.getApplicationContext();
    }
    
    /**
     * <b>IAppContextFinder.getRootAppContext</b>: Returns the application context for the current
     * scope. Since a single level application context is used in the default implementation, this
     * returns the same value as getAppContext();
     */
    @Override
    public ApplicationContext getRootAppContext() {
        return getAppContext();
    }
    
}
