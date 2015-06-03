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

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for implementing a data filter.
 *
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryFilter<T> implements IQueryFilter<T> {
    
    private final Set<IQueryFilterChanged<T>> listeners = new HashSet<>();
    
    public AbstractQueryFilter() {
    }
    
    /**
     * Create an instance with its listener.
     * 
     * @param listener Listener for query filter change events.
     */
    public AbstractQueryFilter(IQueryFilterChanged<T> listener) {
        addListener(listener);
    }
    
    /**
     * Adds a listener.
     * 
     * @param listener Listener for query filter change events.
     */
    @Override
    public void addListener(IQueryFilterChanged<T> listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * 
     * @param listener Listener for query filter change events.
     */
    @Override
    public void removeListener(IQueryFilterChanged<T> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Convenience method for notifying a listener (if any) of a query filter change event.
     */
    protected void notifyListeners() {
        notifyListeners(this);
    }
    
    /**
     * Convenience method for notifying a listener (if any) of a query filter change event.
     */
    protected void notifyListeners(IQueryFilter<T> filter) {
        for (IQueryFilterChanged<T> listener : listeners) {
            listener.onFilterChanged(filter);
        }
    }
    
}
