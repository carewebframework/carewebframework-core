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
        this.strategy = strategy;
    }
    
    @Override
    public IAbortable fetch(IQueryContext context, IQueryCallback<T> callback) {
        context.reset();
        
        if (strategy != null) {
            return strategy.fetch(this, context, callback);
        }
        
        /*
         * A null asynchronous strategy invokes the service synchronously, performing callbacks
         * just as a real asynchronous strategy would do, but returning null for the IAbortable
         * instance.
         */
        callback.onQueryStart(null);
        callback.onQueryFinish(null, fetch(context));
        return null;
    }
    
}
