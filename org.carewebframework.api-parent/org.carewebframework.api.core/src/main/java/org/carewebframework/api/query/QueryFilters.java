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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of data filters with methods for invoking them in batch mode.
 *
 * @param <T> Class of query result.
 */
public class QueryFilters<T> implements IQueryFilterChanged<T> {
    
    private final List<IQueryFilter<T>> filters = new ArrayList<>();
    
    private final List<IQueryFilterChanged<T>> listeners = new ArrayList<>();
    
    /**
     * Register a data filter.
     * 
     * @param filter A data filter.
     * @return True if the operation was successful.
     */
    public boolean registerFilter(IQueryFilter<T> filter) {
        filter.setListener(this);
        return filters.add(filter);
    }
    
    /**
     * Unregister a data filter.
     * 
     * @param filter A data filter.
     * @return True if the operation was successful.
     */
    public boolean unregisterFilter(IQueryFilter<T> filter) {
        filter.setListener(null);
        return filters.remove(filter);
    }
    
    /**
     * Register a filter change listener
     * 
     * @param listener A filter change listener.
     * @return True if the operation was successful.
     */
    public boolean registerListener(IQueryFilterChanged<T> listener) {
        return listeners.add(listener);
    }
    
    /**
     * Unregister a filter change listener
     * 
     * @param listener A filter change listener.
     * @return True if the operation was successful.
     */
    public boolean unregisterListener(IQueryFilterChanged<T> listener) {
        return listeners.remove(listener);
    }
    
    /**
     * Returns true if the result passes all filters.
     * 
     * @param result Result to test.
     * @return True if the result passed all filters.
     */
    public boolean include(T result) {
        boolean include = true;
        
        for (IQueryFilter<T> dataFilter : filters) {
            include &= dataFilter.include(result);
            
            if (!include) {
                break;
            }
        }
        
        return include;
    }
    
    /**
     * Filters a list of results based on the registered filters.
     * 
     * @param results Result list to filter.
     * @return The filtered list. Note that if no results are filtered, the original list is
     *         returned.
     */
    public List<T> filter(List<T> results) {
        if (filters.isEmpty() || results == null) {
            return results;
        }
        
        List<T> include = new ArrayList<>();
        
        for (T result : results) {
            if (include(result)) {
                include.add(result);
            }
        }
        
        return results.size() == include.size() ? results : include;
    }
    
    /**
     * Allows each registered filter to update the service context.
     * 
     * @param context The service context.
     * @return True if the context was modified.
     */
    public boolean updateContext(IQueryContext context) {
        boolean result = false;
        
        for (IQueryFilter<T> filter : filters) {
            result |= filter.updateContext(context);
        }
        
        return result;
    }
    
    /**
     * Convey filter state change to all listeners.
     * 
     * @param filter Filter whose state has changed.
     */
    @Override
    public void onFilterChanged(IQueryFilter<T> filter) {
        for (IQueryFilterChanged<T> listener : listeners) {
            listener.onFilterChanged(filter);
        }
        
    }
    
}
