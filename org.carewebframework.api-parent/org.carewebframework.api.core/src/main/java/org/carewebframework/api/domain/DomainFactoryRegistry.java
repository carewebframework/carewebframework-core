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

import org.carewebframework.api.spring.BeanRegistry;

/**
 * Tracks all domain factory implementations.
 */
@SuppressWarnings("rawtypes")
public class DomainFactoryRegistry extends BeanRegistry<String, IDomainFactory> {
    
    
    private static DomainFactoryRegistry instance = new DomainFactoryRegistry();
    
    public static DomainFactoryRegistry getInstance() {
        return instance;
    }
    
    /**
     * Creates a new instance of an object of this domain.
     * 
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @return The new domain object instance.
     */
    public static <T> T newObject(Class<T> clazz) {
        return getFactory(clazz).newObject(clazz);
    }
    
    /**
     * Fetches an object, identified by its unique id, from the underlying data store.
     *
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @param id Unique id of the object.
     * @return The requested object.
     */
    public static <T> T fetchObject(Class<T> clazz, String id) {
        return getFactory(clazz).fetchObject(clazz, id);
    }
    
    /**
     * Fetches multiple domain objects as specified by an array of identifier values.
     *
     * @param <T> Class of domain object.
     * @param clazz Class of object to create.
     * @param ids An array of unique identifiers.
     * @return A list of domain objects in the same order as requested in the ids parameter.
     */
    public static <T> List<T> fetchObjects(Class<T> clazz, String[] ids) {
        return getFactory(clazz).fetchObjects(clazz, ids);
    }
    
    /**
     * Returns a domain factory for the specified class.
     * 
     * @param <T> Class of domain object.
     * @param clazz Class of object created by factory.
     * @return A domain object factory.
     */
    @SuppressWarnings("unchecked")
    public static <T> IDomainFactory<T> getFactory(Class<T> clazz) {
        for (IDomainFactory<?> factory : instance) {
            String alias = factory.getAlias(clazz);
            
            if (alias != null) {
                return (IDomainFactory<T>) factory;
            }
        }
        
        throw new IllegalArgumentException("Domain class has no registered factory: " + clazz.getName());
    }
    
    private DomainFactoryRegistry() {
        super(IDomainFactory.class);
    }
    
    @Override
    protected String getKey(IDomainFactory item) {
        return item.getClass().getName();
    }
    
}
