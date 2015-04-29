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
 * Filter criterion for responding to a ping request.
 */
public class PingFilter implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public enum PingFilterType {
        APP_NAME, SENTINEL_EVENT
    };
    
    public final PingFilterType type;
    
    public final String value;
    
    @JsonCreator
    public PingFilter(@JsonProperty("type") PingFilterType type, @JsonProperty("value") String value) {
        this.type = type;
        this.value = value;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof PingFilter) {
            PingFilter filter = (PingFilter) object;
            return filter.type == type && filter.value.equals(value);
        }
        
        return false;
    }
}
