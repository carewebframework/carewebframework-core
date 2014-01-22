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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a ping request.
 */
public class PingRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public final String appName;
    
    public final String requestor;
    
    @JsonCreator
    public PingRequest(@JsonProperty("appName") String appName, @JsonProperty("requestor") String requestor) {
        this.appName = appName;
        this.requestor = requestor;
    }
}
