/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.messaging;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Message wrapper.
 */
public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String type;
    
    private final Object payload;
    
    private final String id;
    
    private final long created;
    
    private Map<String, Object> metadata;
    
    /**
     * Creates a message.
     * 
     * @param type The type of the message.
     * @param payload The associated payload.
     */
    public Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.id = UUID.randomUUID().toString();
        this.created = System.currentTimeMillis();
    }
    
    /**
     * @return Returns the type of message. If a type was not specified, it defaults to the channel
     *         name.
     */
    public String getType() {
        return type;
    }
    
    /**
     * @return Returns the payload.
     */
    public Object getPayload() {
        return payload;
    }
    
    /**
     * Returns the payload coerced to the specified type.
     * 
     * @param type The class of the returned data.
     * @return The associated data.
     */
    @SuppressWarnings("unchecked")
    public <T> T getPayload(Class<T> type) {
        return (T) payload;
    }
    
    /**
     * @return The id unique to this message.
     */
    public String getId() {
        return id;
    }
    
    /**
     * @return The timestamp reflecting when the message was created.
     */
    public Date getCreated() {
        return new Date(created);
    }
    
    /**
     * @return True if metadata are present.
     */
    public boolean hasMetadata() {
        return metadata != null && metadata.size() > 0;
    }
    
    /**
     * Returns the metadata map, creating it if not already so.
     * 
     * @return The metadata map.
     */
    private Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        return metadata;
    }
    
    /**
     * Returns a metadata value.
     * 
     * @param key The key.
     * @return The value or null if not present.
     */
    public Object getMetadata(String key) {
        return metadata == null ? null : metadata.get(key);
    }
    
    /**
     * Sets a metadata value.
     * 
     * @param key The key.
     * @param value The value.
     */
    public void setMetadata(String key, Object value) {
        getMetadata().put(key, value);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // @formatter:off
        sb.append("Type: ").append(doFormat(type))
            .append("; id: ").append(id)
            .append("; created: ").append(getCreated())
            .append("; metadata: ").append(doFormat(metadata))
            .append("; payload: ").append(doFormat(payload));
        // @formatter:on
        return sb.toString();
    }
    
    private Object doFormat(Object object) {
        return object == null ? "none" : object;
    }
    
    @Override
    public boolean equals(Object object) {
        return object == this || (object instanceof Message && ((Message) object).id.equals(id));
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
