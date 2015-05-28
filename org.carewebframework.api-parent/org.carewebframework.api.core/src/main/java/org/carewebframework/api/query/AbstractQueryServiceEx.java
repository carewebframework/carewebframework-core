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

/**
 * Abstract service wrapper for a data provider.
 *
 * @param <S> Native data provider service.
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryServiceEx<S, T> extends AbstractQueryService<T> {
    
    protected final S service;
    
    public AbstractQueryServiceEx(S service) {
        super();
        this.service = service;
    }
    
    public AbstractQueryServiceEx(S service, IAsyncQueryStrategy<T> strategy) {
        super(strategy);
        this.service = service;
    }
    
}
