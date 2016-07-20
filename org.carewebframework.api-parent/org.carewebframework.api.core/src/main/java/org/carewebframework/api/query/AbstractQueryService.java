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

import org.carewebframework.api.thread.IAbortable;

/**
 * Base class for implementing a data query service implementing both synchronous and asynchronous
 * fetch modes. Asynchronous support is delegated to the specified asynchronous query strategy. In
 * the absence of an asynchronous query strategy, the asynchronous operation is performed
 * synchronously and the result reported immediately to the callback listener. The default strategy
 * is the ThreadedQueryStrategy, which will work for most implementations.
 *
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryService<T> implements IQueryService<T> {
    
    /**
     * A null asynchronous strategy invokes the service synchronously, performing callbacks just as
     * a real asynchronous strategy would do, but returning null for the IAbortable instance.
     */
    private class NullAsyncQueryStrategy implements IAsyncQueryStrategy<T> {
        
        @Override
        public IAbortable fetch(IQueryService<T> service, IQueryContext context, IQueryCallback<T> callback) {
            callback.onQueryStart(null);
            callback.onQueryFinish(null, service.fetch(context));
            return null;
        }
        
    };
    
    private final IAsyncQueryStrategy<T> strategy;
    
    /**
     * Create the query service using the default async query strategy.
     */
    public AbstractQueryService() {
        this(new ThreadedQueryStrategy<T>());
    }
    
    /**
     * Create the query service using the specified async query strategy.
     * 
     * @param strategy The async query strategy. If null, an asynchronous query operation is
     *            simulated using a synchronous fetch invocation.
     */
    public AbstractQueryService(IAsyncQueryStrategy<T> strategy) {
        this.strategy = strategy == null ? new NullAsyncQueryStrategy() : strategy;
    }
    
    @Override
    public IAbortable fetch(IQueryContext context, IQueryCallback<T> callback) {
        context.reset();
        return strategy.fetch(this, context, callback);
    }
    
}
