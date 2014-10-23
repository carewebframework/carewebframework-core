/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring.mock;

import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.mock.MockUser;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractAuthenticationProvider;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 */
public class MockAuthenticationProvider extends AbstractAuthenticationProvider {
    
    private String mockAuthorities;
    
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
    
    @Override
    protected IUser authenticate(String username, String password, ISecurityDomain domain) {
        if (!check("mock.username", username) || !check("mock.password", password)) {
            throw new BadCredentialsException("Authentication failed.");
        }
        
        return new MockUser(SpringUtil.getProperty("mock.userid"), SpringUtil.getProperty("mock.fullname"), username, domain);
    }
    
    protected boolean check(String property, String value) {
        return value.equals(SpringUtil.getProperty(property));
    }
    
    @Override
    protected List<String> getAuthorities(IUser user) {
        return user == null ? null : StrUtil.toList(mockAuthorities, ",");
    }
    
    public void setMockAuthorities(String mockAuthorities) {
        this.mockAuthorities = mockAuthorities;
    }
    
}
