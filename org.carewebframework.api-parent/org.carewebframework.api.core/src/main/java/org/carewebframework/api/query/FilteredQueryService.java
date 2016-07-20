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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.thread.IAbortable;

/**
 * Wraps a query service implementation to provide filtering of query results.
 *
 * @param <T> Class of query result.
 */
public class FilteredQueryService<T> implements IQueryService<T> {
    
    /**
     * Intermediate callback that permits filtering of the data before invoking the real callback.
     */
    private class QueryCallback implements IQueryCallback<T> {
        
        private final IQueryCallback<T> realCallback;
        
        public QueryCallback(IQueryCallback<T> realCallback) {
            this.realCallback = realCallback;
        }
        
        @Override
        public void onQueryStart(IAbortable thread) {
            realCallback.onQueryStart(thread);
        }
        
        @Override
        public void onQueryFinish(IAbortable thread, IQueryResult<T> result) {
            realCallback.onQueryFinish(thread, filteredResult(result));
        }
        
    }
    
    private final IQueryService<T> service;
    
    private final QueryFilterSet<T> filters;
    
    /**
     * Create an instance using the specified query service and filter set.
     * 
     * @param service The wrapped query service.
     * @param filters The query filter set.
     */
    public FilteredQueryService(IQueryService<T> service, QueryFilterSet<T> filters) {
        this.service = service;
        this.filters = filters;
    }
    
    /**
     * Repackages the query result as the filtered result with the unfiltered version stored in the
     * metadata under the "unfiltered" key.
     * 
     * @param unfilteredResult The unfiltered query result.
     * @return The filtered query result.
     */
    private IQueryResult<T> filteredResult(IQueryResult<T> unfilteredResult) {
        List<T> unfilteredList = unfilteredResult.getResults();
        List<T> filteredList = unfilteredList == null ? null : filters.filter(unfilteredList);
        Map<String, Object> metadata = Collections.<String, Object> singletonMap("unfiltered", unfilteredResult);
        return QueryUtil.packageResult(filteredList, unfilteredResult.getStatus(), metadata);
    }
    
    @Override
    public boolean hasRequired(IQueryContext context) {
        return service.hasRequired(context);
    }
    
    @Override
    public IQueryResult<T> fetch(IQueryContext context) {
        filters.updateContext(context);
        return filteredResult(service.fetch(context));
    }
    
    @Override
    public IAbortable fetch(IQueryContext context, IQueryCallback<T> callback) {
        filters.updateContext(context);
        return service.fetch(context, new QueryCallback(callback));
    }
    
}
