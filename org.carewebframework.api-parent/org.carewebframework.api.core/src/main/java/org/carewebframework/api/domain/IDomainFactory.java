/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
