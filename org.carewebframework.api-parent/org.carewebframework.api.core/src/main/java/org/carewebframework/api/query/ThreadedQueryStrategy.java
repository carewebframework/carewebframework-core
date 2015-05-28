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
import org.carewebframework.api.thread.ThreadUtil;

/**
 * Implements a fetch strategy based on a simple background thread.
 *
 * @param <T> Class of query result.
 */
public class ThreadedQueryStrategy<T> implements IAsyncQueryStrategy<T> {
    
    private class Query extends Thread implements IAbortable {
        
        private final IQueryService<T> service;
        
        private final IQueryContext context;
        
        private final IQueryCallback<T> callback;
        
        private boolean abort;
        
        private Query(IQueryService<T> service, IQueryContext context, IQueryCallback<T> callback) {
            this.service = service;
            this.context = context;
            this.callback = callback;
        }
        
        @Override
        public void run() {
            IQueryResult<T> result;
            
            try {
                result = service.fetch(context);
            } catch (Throwable t) {
                result = QueryUtil.<T> errorResult(t);
            }
            
            callback.onQueryFinish(this, abort ? QueryUtil.<T> abortResult(null) : result);
        }
        
        @Override
        public void abort() {
            abort = true;
        }
    }
    
    public ThreadedQueryStrategy() {
        
    }
    
    @Override
    public IAbortable fetch(IQueryService<T> service, IQueryContext context, IQueryCallback<T> callback) {
        Query query = new Query(service, context, callback);
        ThreadUtil.startThread(query);
        return query;
    }
}
