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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single recipient of a published message. Use to constrain message delivery to
 * specific subscribers of a topic.
 */
public class Recipient implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // @formatter:off
    public enum RecipientType {
        OTHER,          // Other recipient type
        APPLICATION,    // Application by name
        USER,           // User by unique id
        SESSION,        // An application session by id
        CONSUMER        // Consumer by node id
    }
    // @formatter:on
    
    private final RecipientType type;
    
    private final String value;
    
    @JsonCreator
    public Recipient(@JsonProperty("type") RecipientType type, @JsonProperty("value") String value) {
        this.type = type;
        this.value = value;
    }
    
    public RecipientType getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() + value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        Recipient recp = obj instanceof Recipient ? (Recipient) obj : null;
        return recp == this || (recp != null && recp.type == type && recp.value.equals(value));
    }
}
