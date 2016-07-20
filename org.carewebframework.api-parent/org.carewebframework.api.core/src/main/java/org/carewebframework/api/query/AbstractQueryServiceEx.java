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

/**
 * A convenience extension to the AbstractQueryService for query services that use an underlying
 * data provider service.
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
