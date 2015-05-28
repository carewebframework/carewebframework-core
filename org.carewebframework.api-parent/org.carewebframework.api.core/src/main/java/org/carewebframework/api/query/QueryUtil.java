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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.query.IQueryResult.CompletionStatus;

/**
 * Static utility methods.
 */
public class QueryUtil {
    
    private static class QueryResult<T> implements IQueryResult<T> {
        
        private final List<T> results;
        
        private final CompletionStatus status;
        
        private final Map<String, Object> metadata;
        
        private QueryResult(List<T> results, CompletionStatus status, Map<String, Object> metadata) {
            this.results = results;
            this.status = status == null ? CompletionStatus.COMPLETED : status;
            this.metadata = metadata;
        }
        
        @Override
        public CompletionStatus getStatus() {
            return status;
        }
        
        @Override
        public List<T> getResults() {
            return results == null ? Collections.<T> emptyList() : results;
        }
        
        @Override
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
        
    }
    
    /**
     * Returns a query result for an aborted operation.
     * 
     * @param reason Optional reason for the aborted operation.
     * @return Query result.
     */
    public static <T> IQueryResult<T> abortResult(String reason) {
        return packageResult(null, CompletionStatus.ABORTED,
            reason == null ? null : Collections.singletonMap("reason", (Object) reason));
    }
    
    /**
     * Returns a query result for an error.
     * 
     * @param exception The exception being reported.
     * @return Query result.
     */
    public static <T> IQueryResult<T> errorResult(Throwable exception) {
        return packageResult(null, CompletionStatus.ERROR,
            exception == null ? null : Collections.singletonMap("exception", (Object) exception));
    }
    
    /**
     * Convenience method for packaging query results.
     *
     * @param results Results to package.
     * @return Packaged results.
     */
    public static <T> IQueryResult<T> packageResult(List<T> results) {
        return packageResult(results, null);
    }
    
    /**
     * Convenience method for packaging query results.
     *
     * @param results Results to package.
     * @param status The completion status.
     * @return Packaged results.
     */
    public static <T> IQueryResult<T> packageResult(List<T> results, CompletionStatus status) {
        return packageResult(results, status, null);
    }
    
    /**
     * Convenience method for packaging query results.
     *
     * @param results Results to package.
     * @param status The completion status.
     * @param metadata Additional metadata.
     * @return Packaged results and metadata.
     */
    public static <T> IQueryResult<T> packageResult(List<T> results, CompletionStatus status, Map<String, Object> metadata) {
        return new QueryResult<T>(results, status, metadata);
    }
    
    /**
     * Force static class.
     */
    private QueryUtil() {
    };
}
