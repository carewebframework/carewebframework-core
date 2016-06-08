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

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Returns information about a global event publisher.
 */
@JsonDeserialize(as = PublisherInfo.class)
public interface IPublisherInfo {
    
    /**
     * Returns the user's domain id.
     */
    String getUserId();
    
    /**
     * Returns the full name of the user.
     */
    String getUserName();
    
    /**
     * Returns the application name.
     */
    String getAppName();
    
    /**
     * Returns the unique id of the message producer's node.
     */
    String getProducerId();
    
    /**
     * Returns the unique id of the message consumer's node.
     */
    String getConsumerId();
    
    /**
     * Returns the unique id of an application instance.
     */
    String getSessionId();
    
    /**
     * Returns a map of message routing attributes.
     */
    Map<String, String> getAttributes();
    
}
