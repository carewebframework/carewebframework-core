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
 * Base class for implementing a synchronous and asynchronous data service. Asynchronous support is
 * delegated to the specified asynchronous query strategy. In the absence of an asynchronous query
 * strategy, the asynchronous operation is performed synchronously and the result reported
 * immediately to the callback listener. The default strategy is the ThreadedQueryStrategy.
 *
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryService<T> implements IQueryService<T> {
    
    private final IAsyncQueryStrategy<T> strategy;
    
    public AbstractQueryService() {
        this(new ThreadedQueryStrategy<T>());
    }
    
    public AbstractQueryService(IAsyncQueryStrategy<T> strategy) {
        this.strategy = strategy;
    }
    
    @Override
    public IAbortable fetch(IQueryContext context, IQueryCallback<T> callback) {
        context.reset();
        
        if (strategy != null) {
            return strategy.fetch(this, context, callback);
        }
        
        callback.onQueryFinish(null, fetch(context));
        return null;
    }
    
}
