/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.api.security.mock;

import java.util.List;
import java.util.Map;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;

/**
 * Mock user for testing.
 */
public class MockSecurityDomain implements ISecurityDomain {
    
    private static final long serialVersionUID = 1L;
    
    private final String logicalId;
    
    private final String name;
    
    private String mockAuthorities;
    
    private final Map<String, String> attributes;
    
    public MockSecurityDomain() {
        this("mockId", "mockDomain");
    }
    
    public MockSecurityDomain(String logicalId, String name) {
        this(logicalId, name, null);
    }
    
    public MockSecurityDomain(String logicalId, String name, Map<String, String> attributes) {
        this.logicalId = logicalId;
        this.name = name;
        this.attributes = attributes;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    @Override
    public String getAttribute(String name) {
        return attributes == null ? null : attributes.get(name);
    }
    
    @Override
    public IUser authenticate(String username, String password) {
        if (!check("mock.username", username) || !check("mock.password", password)) {
            throw new RuntimeException("Authentication failed.");
        }
        
        return new MockUser(SpringUtil.getProperty("mock.userid"), SpringUtil.getProperty("mock.fullname"), username,
                password, this);
    }
    
    @Override
    public MockSecurityDomain getNativeSecurityDomain() {
        return this;
    }
    
    @Override
    public List<String> getGrantedAuthorities(IUser user) {
        return StrUtil.toList(mockAuthorities, ",");
    }
    
    public void setMockAuthorities(String mockAuthorities) {
        this.mockAuthorities = mockAuthorities;
    }
    
    protected boolean check(String property, String value) {
        return value.equals(SpringUtil.getProperty(property));
    }
    
}
