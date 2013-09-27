/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.mock.security.impl;

import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.security.spring.AbstractAuthenticationProvider;
import org.carewebframework.security.spring.CWFAuthenticationDetails;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 * 
 * @author dmartin
 */
public class MockAuthenticationProvider extends AbstractAuthenticationProvider {
    
    public MockAuthenticationProvider() {
        super(false);
    }
    
    protected MockAuthenticationProvider(boolean debugRole) {
        super(debugRole);
    }
    
    protected MockAuthenticationProvider(List<String> grantedAuthorities) {
        super(grantedAuthorities);
    }
    
    /**
     * Performs a user login.
     * 
     * @param details Authentication details
     * @param username Username for the login.
     * @param password Password for the login (ignored if the user is pre-authenticated).
     * @param authority Authority for which the login is requested.
     * @return Authorization result
     */
    @Override
    protected IUser login(CWFAuthenticationDetails details, String username, String password, String authority) {
        IUser user = authenticate(username, password, authority);
        details.setDetail("user", user);
        return user;
    }
    
    private IUser authenticate(final String username, final String password, final String authority) {
        if (!check("mock.username", username) || !check("mock.password", password) || !check("mock.authority", authority)) {
            throw new BadCredentialsException("Authentication failed.");
        }
        
        return new IUser() {
            
            long id = 123;
            
            String uname = username;
            
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
                return PropertyUtil.getValue("mock.fullname", null);
            }
            
        };
    }
    
    private boolean check(String property, String value) {
        return value.equals(PropertyUtil.getValue(property, null));
    }
    
    @Override
    protected List<String> getPrivileges(IUser user) {
        return user == null ? null : PropertyUtil.getValues("mock.privileges", null);
    }
    
}
