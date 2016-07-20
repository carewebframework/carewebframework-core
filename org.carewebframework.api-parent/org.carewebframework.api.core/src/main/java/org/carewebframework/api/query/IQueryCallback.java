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
