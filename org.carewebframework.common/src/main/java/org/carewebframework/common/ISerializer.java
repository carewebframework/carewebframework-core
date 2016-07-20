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
package org.carewebframework.common;

/**
 * Methods for serializing / deserializing an object.
 * 
 * @param <T> Class of object being serialized or deserialized.
 */
public interface ISerializer<T> {
    
    /**
     * Serialize an object to its string form.
     * 
     * @param object Object instance to serialize.
     * @return Serialized form of object.
     */
    String serialize(T object);
    
    /**
     * Deserialize an object from its string form.
     * 
     * @param value Serialized form of object.
     * @return Deserialized object instance.
     */
    T deserialize(String value);
    
    /**
     * Get class type of target object.
     * 
     * @return Class type of target object.
     */
    Class<T> getType();
    
}
