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
 * Callback interface used by a query invoked in asynchronous mode.
 *
 * @param <T> Class of query result.
 */
public interface IQueryCallback<T> {
    
    /**
     * Called when the asynchronous query has just been started.
     * 
     * @param thread An IAbortable instance which can be used to abort the query in progress. Note
     *            that this can be null if the underlying query service does not support an
     *            abortable asynchronous operation.
     */
    void onQueryStart(IAbortable thread);
    
    /**
     * Called when the asynchronous query has terminated, whether by normal completion or by an
     * error or abort request.
     * 
     * @param thread The IAbortable instance associated with the asynchronous query. This may be
     *            null (see {@link #onQueryStart(IAbortable)}).
     * @param result The result of the asynchronous query.
     */
    void onQueryFinish(IAbortable thread, IQueryResult<T> result);
    
}
