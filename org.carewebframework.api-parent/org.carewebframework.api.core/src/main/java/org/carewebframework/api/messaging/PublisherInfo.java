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
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.messaging.Recipient.RecipientType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for holding information about a publisher.
 */
public class PublisherInfo implements IPublisherInfo, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String userName;
    
    private final Map<String, String> attributes = new HashMap<>();
    
    public PublisherInfo() {
        
    }
    
    @JsonCreator
    public PublisherInfo(@JsonProperty("userName") String userName,
        @JsonProperty("attributes") Map<String, String> attributes) {
        this.userName = userName;
        this.attributes.putAll(attributes);
    }
    
    public PublisherInfo(IPublisherInfo publisherInfo) {
        this(publisherInfo.getUserName(), publisherInfo.getAttributes());
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
        return get(RecipientType.USER);
    }
    
    @JsonIgnore
    public void setUserId(String userId) {
        put(RecipientType.USER, userId);
    }
    
    @Override
    public String getAppName() {
        return get(RecipientType.APPLICATION);
    }
    
    @JsonIgnore
    public void setAppName(String appName) {
        put(RecipientType.APPLICATION, appName);
    }
    
    @Override
    public String getProducerId() {
        return attributes.get("cwf-PRODUCER");
    }
    
    @JsonIgnore
    public void setProducerId(String nodeId) {
        attributes.put("cwf-PRODUCER", nodeId);
    }
    
    @Override
    public String getConsumerId() {
        return get(RecipientType.CONSUMER);
    }
    
    @JsonIgnore
    public void setConsumerId(String nodeId) {
        put(RecipientType.CONSUMER, nodeId);
    }
    
    @Override
    public String getSessionId() {
        return get(RecipientType.SESSION);
    }
    
    @JsonIgnore
    public void setSessionId(String nodeId) {
        put(RecipientType.SESSION, nodeId);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof IPublisherInfo && attributes.equals(((IPublisherInfo) obj).getAttributes()));
    }
    
    @Override
    public int hashCode() {
        return attributes.hashCode();
    }
    
    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public String get(RecipientType recipientType) {
        return attributes.get("cwf-" + recipientType.name());
    }
    
    public void put(RecipientType recipientType, String value) {
        attributes.put("cwf-" + recipientType.name(), value);
    }
}
