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
package org.carewebframework.api.domain;

import java.util.List;

/**
 * Interface for a domain object factory.
 * 
 * @param <D> Base class created by factory.
 */
public interface IDomainFactory<D> {
    
    
    /**
     * Creates a new instance of an object of this domain.
     * 
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @return The new domain object instance.
     */
    <T extends D> T newObject(Class<T> clazz);
    
    /**
     * Fetches an object, identified by its unique id, from the underlying data store.
     *
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @param id Unique id of the object.
     * @return The requested object.
     */
    <T extends D> T fetchObject(Class<T> clazz, String id);
    
    /**
     * Fetches multiple domain objects as specified by an array of identifier values.
     *
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @param ids An array of unique identifiers.
     * @return A list of domain objects in the same order as requested in the ids parameter.
     */
    <T extends D> List<T> fetchObjects(Class<T> clazz, String[] ids);
    
    /**
     * Returns the alias for the domain class.
     *
     * @param clazz Domain class whose alias is sought.
     * @return The alias for the domain class, or null if class not supported by this factory.
     */
    String getAlias(Class<?> clazz);
}
