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

import org.carewebframework.api.thread.IAbortable;

/**
 * Base class for executing a background service and optionally filtering the result.
 *
 * @param <T> Class of query result.
 */
public class FilteredQuery<T> implements IQueryFilterChanged<T>, IQueryCallback<T> {
    
    /**
     * Wraps the query result with a filtered version of the same.
     */
    private class FilteredQueryResult implements IQueryResult<T> {
        
        private List<T> filteredResults;
        
        @Override
        public List<T> getResults() {
            return filteredResults;
        }
        
        @Override
        public Object getMetadata(String key) {
            return queryResult.getMetadata(key);
        }
        
        @Override
        public IQueryResult.CompletionStatus getStatus() {
            return queryResult.getStatus();
        }
        
        private IQueryResult<T> applyFilters() {
            filteredResults = filters.filter(queryResult.getResults());
            return this;
        }
    }
    
    private final IQueryService<T> service;
    
    private final QueryFilters<T> filters;
    
    private final List<IQueryCallback<T>> callbacks = new ArrayList<>();
    
    private final FilteredQueryResult filteredResult = new FilteredQueryResult();
    
    private final IQueryContext context;
    
    private IQueryResult<T> queryResult;
    
    private IAbortable query;
    
    public FilteredQuery(IQueryService<T> service, IQueryContext context, QueryFilters<T> filters) {
        this.service = service;
        this.context = context;
        this.filters = filters;
        filters.registerListener(this);
    }
    
    public void registerCallback(IQueryCallback<T> callback) {
        callbacks.add(callback);
    }
    
    @Override
    public void onFilterChanged(IQueryFilter<T> filter) {
        if (filter.updateContext(context) && context.hasChanged()) {
            queryResult = null;
            
            if (query != null) {
                query.abort();
            }
            
            query = service.fetch(context, this);
            onQueryStart(query);
        } else {
            onQueryFinish(query, filteredResult.applyFilters());
        }
    }
    
    @Override
    public void onQueryStart(IAbortable thread) {
        for (IQueryCallback<T> callback : callbacks) {
            callback.onQueryStart(thread);
        }
    }
    
    @Override
    public void onQueryFinish(IAbortable thread, IQueryResult<T> result) {
        this.queryResult = result;
        filteredResult.applyFilters();
        
        for (IQueryCallback<T> callback : callbacks) {
            callback.onQueryFinish(thread, filteredResult);
        }
    }
    
}
