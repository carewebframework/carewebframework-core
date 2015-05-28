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
 * Strategy for fetching data asynchronously.
 *
 * @param <T> Class of query result.
 */
public interface IAsyncQueryStrategy<T> {
    
    /**
     * Asynchronously fetches data from a data query service.
     * 
     * @param service A data query service.
     * @param context The service context that supplies the query parameters.
     * @param callback The callback to report the query result.
     * @return An object implementing IAbortable, or null if no such implementation is available.
     */
    IAbortable fetch(IQueryService<T> service, IQueryContext context, IQueryCallback<T> callback);
    
}
