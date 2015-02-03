/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
