/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

import java.io.Serializable;

import org.carewebframework.api.messaging.Message;

/**
 * Wraps an event into a message for global delivery.
 */
public class EventMessage extends Message {
    
    public EventMessage(String eventName, Serializable eventData) {
        super(EventUtil.getChannelName(eventName), eventName, eventData);
    }
    
}
