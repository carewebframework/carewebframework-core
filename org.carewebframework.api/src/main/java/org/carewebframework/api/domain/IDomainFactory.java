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
 * @param <T> The class of domain object serviced by the factory.
 */
public interface IDomainFactory<T extends IDomainObject> {
    
    /**
     * Creates a new instance of an object of this domain.
     * 
     * @return The new domain object instance.
     */
    T newObject();
    
    /**
     * Fetches an object, identified by its unique id, from the underlying data store.
     * 
     * @param id Unique id of the object.
     * @return The requested object.
     */
    T fetchObject(long id);
    
    /**
     * Fetches multiple domain objects as specified by an array of identifier values.
     * 
     * @param ids An array of unique identifiers.
     * @return A list of domain objects in the same order as requested in the ids parameter.
     */
    List<T> fetchObjects(long[] ids);
    
}
