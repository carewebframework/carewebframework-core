/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.chat;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.carewebframework.api.event.IPublisherInfo;

/**
 * A single chat message.
 */
public class ChatMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public final IPublisherInfo sender;
    
    public final Date timestamp;
    
    public final String message;
    
    protected ChatMessage(IPublisherInfo sender, String message) {
        this(sender, message, new Date());
    }
    
    @JsonCreator
    protected ChatMessage(@JsonProperty("sender") IPublisherInfo sender, @JsonProperty("message") String message,
        @JsonProperty("timestamp") Date timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }
}
