/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.context;

/**
 * Methods for serializing / deserializing an object to / from a shared context.
 * 
 * @param <T> Class of object being serialized or deserialized.
 */
public interface IContextSerializer<T> {
    
    String serialize(T object);
    
    T deserialize(String value);
    
    Class<T> getType();
    
}
