/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.testharness.security.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractAuthenticationProvider;
import org.carewebframework.security.spring.CWFAuthenticationDetails;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 */
public class MockAuthenticationProvider extends AbstractAuthenticationProvider {
    
    /**
     * No-arg constructor.
     */
    public MockAuthenticationProvider() {
        super(false);
    }
    
    protected MockAuthenticationProvider(boolean debugRole) {
        super(debugRole);
    }
    
    protected MockAuthenticationProvider(List<String> grantedAuthorities) {
        super(grantedAuthorities);
    }
    
    private static class MockIUser implements IUser, Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private long id = 123;
        
        private String uname;
        
        private String fullName;
        
        public MockIUser(String username) {
            this.uname = username;
        }
        
        @Override
        public long getDomainId() {
            return id;
        }
        
        @Override
        public void setDomainId(long id) {
            this.id = id;
        }
        
        @Override
        public Object getProxiedObject() {
            return this;
        }
        
        @Override
        public void setUsername(String username) {
            uname = username;
        }
        
        @Override
        public String getUsername() {
            return uname;
        }
        
        @Override
        public String getFullName() {
            if (fullName == null) {
                fullName = PropertyUtil.getValue("mock.fullname", null);
            }
            
            return fullName;
        }
        
        @Override
        public String getGender() {
            return null;
        }
        
        @Override
        public Date getBirthDate() {
            return null;
        }
        
        @Override
        public Date getDeathDate() {
            return null;
        }
        
        @Override
        public IInstitution getInstitution() {
            return null;
        }
        
        @Override
        public EntityIdentifier getIdentifier(String sysId) {
            return null;
        }
    }
    
    /**
     * Performs a user login.
     * 
     * @param details Authentication details
     * @param username Username for the login.
     * @param password Password for the login (ignored if the user is pre-authenticated).
     * @param domain Domain for which the login is requested.
     * @return Authorization result
     */
    @Override
    protected IUser login(CWFAuthenticationDetails details, String username, String password, String domain) {
        IUser user = authenticate(username, password, domain);
        details.setDetail("user", user);
        return user;
    }
    
    private IUser authenticate(final String username, final String password, final String domain) {
        if (!check("mock.username", username) || !check("mock.password", password) || !check("mock.domain", domain)) {
            throw new BadCredentialsException("Authentication failed.");
        }
        
        return new MockIUser(username);
    }
    
    private boolean check(String property, String value) {
        return value.equals(PropertyUtil.getValue(property, null));
    }
    
    @Override
    protected List<String> getAuthorities(IUser user) {
        return user == null ? null : StrUtil.toList(PropertyUtil.getValue("mock.authorities", null), ",");
    }
    
}
