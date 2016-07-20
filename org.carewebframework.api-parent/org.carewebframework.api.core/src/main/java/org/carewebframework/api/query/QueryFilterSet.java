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
package org.carewebframework.api.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exposes a set of query filters as a single filter whose methods are invoked on each of the
 * members.
 *
 * @param <T> Class of query result.
 */
public class QueryFilterSet<T> extends AbstractQueryFilter<T> implements IQueryFilterChanged<T> {
    
    private final Set<IQueryFilter<T>> filters = new HashSet<>();
    
    /**
     * Add a data filter.
     * 
     * @param filter A data filter.
     * @return True if the operation was successful.
     */
    public boolean add(IQueryFilter<T> filter) {
        filter.addListener(this);
        return filters.add(filter);
    }
    
    /**
     * Remove a data filter.
     * 
     * @param filter A data filter.
     * @return True if the operation was successful.
     */
    public boolean remove(IQueryFilter<T> filter) {
        filter.removeListener(this);
        return filters.remove(filter);
    }
    
    /**
     * Applied across all filters in the set.
     * <p>
     * {@inheritDoc}
     */
    @Override
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
     * Applied across all filters in the set.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean updateContext(IQueryContext context) {
        boolean result = false;
        
        for (IQueryFilter<T> filter : filters) {
            result |= filter.updateContext(context);
        }
        
        return result;
    }
    
    /**
     * Filters a list of results based on the member filters.
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
    
    @Override
    public void onFilterChanged(IQueryFilter<T> filter) {
        if (filter != this) {
            notifyListeners(filter);
        }
    }
    
}
