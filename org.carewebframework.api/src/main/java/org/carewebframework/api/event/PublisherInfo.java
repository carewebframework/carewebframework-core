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

/**
 * Class for holding information about a publisher.
 */
public class PublisherInfo implements IPublisherInfo, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long userId;
    
    private String userName;
    
    private String appName;
    
    private String endpointId;
    
    public PublisherInfo() {
        
    };
    
    public PublisherInfo(IPublisherInfo publisherInfo) {
        this.userId = publisherInfo.getUserId();
        this.userName = publisherInfo.getUserName();
        this.appName = publisherInfo.getAppName();
        this.endpointId = publisherInfo.getEndpointId();
    }
    
    @Override
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    @Override
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    @Override
    public String getEndpointId() {
        return endpointId;
    }
    
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
    
}
