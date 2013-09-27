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

import org.springframework.context.ApplicationContext;

/**
 * Implementors must define the logic for locating the framework's application context.
 */
public interface IAppContextFinder {
    
    /**
     * Returns the application context for the current scope. If the application implements
     * hierarchical contexts, this will return the lowest order application context in the
     * hierarchy. If not, this will return the same value as getRootAppContext.
     * 
     * @return The application context.
     */
    ApplicationContext getAppContext();
    
    /**
     * Returns the application root context for the current scope. If the application implements
     * hierarchical contexts, this will return the highest order application context in the
     * hierarchy. If not, this will return the same value as getAppContext.
     * 
     * @return The root application context.
     */
    ApplicationContext getRootAppContext();
}
