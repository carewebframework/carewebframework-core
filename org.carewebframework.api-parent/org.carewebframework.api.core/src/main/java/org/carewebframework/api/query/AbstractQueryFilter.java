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
    
    public AbstractQueryFilter(IQueryFilterChanged<T> listener) {
        setListener(listener);
    }
    
    @Override
    public void setListener(IQueryFilterChanged<T> listener) {
        this.listener = listener;
    }
    
    protected void notifyListener() {
        if (listener != null) {
            listener.onFilterChanged(this);
        }
    }
    
}
