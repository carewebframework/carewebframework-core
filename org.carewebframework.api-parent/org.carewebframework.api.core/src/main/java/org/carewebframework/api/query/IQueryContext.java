/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.query;

/**
 * Context information to be passed to data service.
 */
public interface IQueryContext {
    
    /**
     * Sets a query parameter value.
     * 
     * @param name Parameter name
     * @param value Parameter value
     * @return True if the value was changed.
     */
    public boolean setParam(String name, Object value);
    
    /**
     * Gets a query parameter value.
     * 
     * @param name Parameter name
     * @return Parameter value
     */
    public Object getParam(String name);
    
    /**
     * Returns true if the current context state has changed from the previous snapshot.
     * 
     * @return True if the context has changed.
     */
    boolean hasChanged();
    
    /**
     * Clears the change status.
     */
    void reset();
    
}
