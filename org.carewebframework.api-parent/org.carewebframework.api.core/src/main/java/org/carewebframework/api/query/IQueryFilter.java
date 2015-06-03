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
 * All data filters must implement this interface.
 *
 * @param <T> Class of query result.
 */
public interface IQueryFilter<T> {
    
    /**
     * Adds a listener for change events.
     * 
     * @param listener The change event listener.
     */
    void addListener(IQueryFilterChanged<T> listener);
    
    /**
     * Removes a listener for change events.
     * 
     * @param listener The change event listener.
     */
    void removeListener(IQueryFilterChanged<T> listener);
    
    /**
     * Return true to include result in current model.
     *
     * @param result Result object to evaluate.
     * @return True to include result in model.
     */
    boolean include(T result);
    
    /**
     * Allows a filter to update the service context with new filter settings.
     * 
     * @param context The filter context to update
     * @return True if the filter context was updated.
     */
    boolean updateContext(IQueryContext context);
    
}
