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
 * All data filters must implement this interface.
 *
 * @param <T> Class of query result.
 */
public interface IQueryFilter<T> {
    
    /**
     * Adds a listener for change events.
     * 
     * @param listener The change event listener.
     */
    void addListener(IQueryFilterChanged<T> listener);
    
    /**
     * Removes a listener for change events.
     * 
     * @param listener The change event listener.
     */
    void removeListener(IQueryFilterChanged<T> listener);
    
    /**
     * Return true to include result in current model.
     *
     * @param result Result object to evaluate.
     * @return True to include result in model.
     */
    boolean include(T result);
    
    /**
     * Allows a filter to update the service context with new filter settings.
     * 
     * @param context The filter context to update
     * @return True if the filter context was updated.
     */
    boolean updateContext(IQueryContext context);
    
}
