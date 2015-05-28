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

import java.util.List;

/**
 * Results and metadata returned by an IDataService implementation.
 *
 * @param <T> Class of query result.
 */
public interface IQueryResult<T> {
    
    enum CompletionStatus {
        COMPLETED, ABORTED, ERROR
    };
    
    /**
     * Returns the completion status of the query.
     * 
     * @return The completion status.
     */
    CompletionStatus getStatus();
    
    /**
     * Returns the list of results produced by a query.
     * 
     * @return Results produced by a query.
     */
    List<T> getResults();
    
    /**
     * Provides access to metadata produced by a query.
     * 
     * @param key Key of metadata element.
     * @return Request metadata element, or null if not found.
     */
    Object getMetadata(String key);
    
}
