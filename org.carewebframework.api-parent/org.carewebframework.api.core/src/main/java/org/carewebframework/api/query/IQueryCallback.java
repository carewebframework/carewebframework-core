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
 * Data retrieval services may either directly implement this interface or be wrapped by a class
 * that implements the interface.
 *
 * @param <T> Class of query result.
 */
public interface IQueryCallback<T> {
    
    void onQueryStart(IAbortable thread);
    
    void onQueryFinish(IAbortable thread, IQueryResult<T> result);
    
}
