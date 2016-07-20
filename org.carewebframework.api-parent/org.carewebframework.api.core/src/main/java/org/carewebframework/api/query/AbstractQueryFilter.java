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

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for implementing a data filter.
 *
 * @param <T> Class of query result.
 */
public abstract class AbstractQueryFilter<T> implements IQueryFilter<T> {
    
    
    private final Set<IQueryFilterChanged<T>> listeners = new HashSet<>();
    
    public AbstractQueryFilter() {
    }
    
    /**
     * Create an instance with its listener.
     * 
     * @param listener Listener for query filter change events.
     */
    public AbstractQueryFilter(IQueryFilterChanged<T> listener) {
        addListener(listener);
    }
    
    /**
     * Adds a listener.
     * 
     * @param listener Listener for query filter change events.
     */
    @Override
    public void addListener(IQueryFilterChanged<T> listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * 
     * @param listener Listener for query filter change events.
     */
    @Override
    public void removeListener(IQueryFilterChanged<T> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Convenience method for notifying a listener (if any) of a query filter change event.
     */
    protected void notifyListeners() {
        notifyListeners(this);
    }
    
    /**
     * Convenience method for notifying a listener (if any) of a query filter change event.
     * 
     * @param filter The filter that changed.
     */
    protected void notifyListeners(IQueryFilter<T> filter) {
        for (IQueryFilterChanged<T> listener : listeners) {
            listener.onFilterChanged(filter);
        }
    }
    
}
