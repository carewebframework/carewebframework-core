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
 * Abstract base class for implementing a data filter.
 *
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryFilter<T> implements IQueryFilter<T> {
    
    private IQueryFilterChanged<T> listener;
    
    public AbstractQueryFilter() {
    }
    
    /**
     * Create an instance with its listener.
     * 
     * @param listener Listener for query filter change events.
     */
    public AbstractQueryFilter(IQueryFilterChanged<T> listener) {
        setListener(listener);
    }
    
    /**
     * Sets the listener.
     * 
     * @param listener Listener for query filter change events.
     */
    @Override
    public void setListener(IQueryFilterChanged<T> listener) {
        this.listener = listener;
    }
    
    /**
     * Convenience method for notifying a listener (if any) of a query filter change event.
     */
    protected void notifyListener() {
        if (listener != null) {
            listener.onFilterChanged(this);
        }
    }
    
}
