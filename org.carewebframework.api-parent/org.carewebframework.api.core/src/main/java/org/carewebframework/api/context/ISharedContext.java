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
package org.carewebframework.api.context;

/**
 * Every managed context must implement this interface to permit access to the wrapped domain
 * object.
 * 
 * @param <DomainClass> This represents the domain class that is wrapped by this managed context.
 */
public interface ISharedContext<DomainClass> {
    
    /**
     * Returns the underlying domain object associated with the current or pending context.
     * 
     * @param pending If true, the domain object associated with the pending context is returned. If
     *            false, the domain object associated with the current context is returned.
     * @return Domain object in the specified context.
     */
    DomainClass getContextObject(boolean pending);
    
    /**
     * Sets the specified domain object into the pending context and invokes the context change
     * sequence.
     * 
     * @param newContextObject Domain object
     * @throws ContextException Context exception.
     */
    void requestContextChange(DomainClass newContextObject) throws ContextException;
}
