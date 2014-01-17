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
import java.util.HashMap;
import java.util.Map;

/**
 * Class for holding information about a publisher.
 */
public class PublisherInfo implements IPublisherInfo, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String userName;
    
    private final Map<String, String> attributes = new HashMap<String, String>();
    
    public PublisherInfo() {
        
    };
    
    public PublisherInfo(IPublisherInfo publisherInfo) {
        this.userName = publisherInfo.getUserName();
        attributes.putAll(publisherInfo.getAttributes());
    }
    
    @Override
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String getUserId() {
        return attributes.get("userId");
    }
    
    public void setUserId(String userId) {
        attributes.put("userId", userId);
    }
    
    @Override
    public String getAppName() {
        return attributes.get("appName");
    }
    
    public void setAppName(String appName) {
        attributes.put("appName", appName);
    }
    
    @Override
    public String getEndpointId() {
        return attributes.get("ep");
    }
    
    public void setEndpointId(String endpointId) {
        attributes.put("ep", endpointId);
    }
    
    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IPublisherInfo && attributes.equals(((IPublisherInfo) obj).getAttributes());
    }
}
