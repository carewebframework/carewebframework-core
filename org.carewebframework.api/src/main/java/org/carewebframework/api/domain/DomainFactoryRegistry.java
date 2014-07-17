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

import org.carewebframework.api.spring.BeanRegistry;

/**
 * Tracks all domain factory implementations.
 */
public class DomainFactoryRegistry extends BeanRegistry<IDomainFactory> {
    
    private static DomainFactoryRegistry instance = new DomainFactoryRegistry();
    
    public static DomainFactoryRegistry getInstance() {
        return instance;
    }
    
    /**
     * Creates a new instance of an object of this domain.
     * 
     * @param clazz Class of object to create.
     * @return The new domain object instance.
     */
    public static <T extends IDomainObject> T newObject(Class<T> clazz) {
        return getFactory(clazz).newObject(clazz);
    }
    
    /**
     * Fetches an object, identified by its unique id, from the underlying data store.
     *
     * @param clazz Class of object to create.
     * @param id Unique id of the object.
     * @return The requested object.
     */
    public static <T extends IDomainObject> T fetchObject(Class<T> clazz, String id) {
        return getFactory(clazz).fetchObject(clazz, id);
    }
    
    /**
     * Fetches an object, identified by its unique key, from the specified table.
     * 
     * @param clazz Class of object to create.
     * @param key The unique key associated with the object.
     * @param table The table containing the requested object.
     * @return The requested object.
     */
    public static <T extends IDomainObject> T fetchObject(Class<T> clazz, String key, String table) {
        return getFactory(clazz).fetchObject(clazz, key, table);
    }
    
    /**
     * Fetches multiple domain objects as specified by an array of identifier values.
     *
     * @param clazz Class of object to create.
     * @param ids An array of unique identifiers.
     * @return A list of domain objects in the same order as requested in the ids parameter.
     */
    public static <T extends IDomainObject> List<T> fetchObjects(Class<T> clazz, String[] ids) {
        return getFactory(clazz).fetchObjects(clazz, ids);
    }
    
    public static IDomainFactory getFactory(Class<? extends IDomainObject> clazz) {
        for (IDomainFactory factory : instance) {
            if (factory.getAlias(clazz) != null) {
                return factory;
            }
        }
        
        throw new IllegalArgumentException("Domain class has no registered factory: " + clazz.getName());
    }
    
    private DomainFactoryRegistry() {
        super(IDomainFactory.class);
    }
    
}
