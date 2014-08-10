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

import org.carewebframework.api.domain.IDomainObject;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractAuthenticationProvider;
import org.carewebframework.security.spring.CWFAuthenticationDetails;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 */
public class MockAuthenticationProvider extends AbstractAuthenticationProvider<IDomainObject> {
    
    public interface IMockUserFactory<T> {
        
        public IDomainObject create(String name);
    }
    
    private String mockAuthorities;
    
    private IMockUserFactory<?> mockUserFactory;
    
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
    protected IDomainObject login(CWFAuthenticationDetails details, String username, String password, String domain) {
        IDomainObject user = authenticate(username, password, domain);
        details.setDetail("user", user);
        return user;
    }
    
    private IDomainObject authenticate(final String username, final String password, final String domain) {
        if (!check("mock.username", username) || !check("mock.password", password) || !check("mock.domain", domain)) {
            throw new BadCredentialsException("Authentication failed.");
        }
        
        return mockUserFactory.create(SpringUtil.getProperty("mock.fullname"));
    }
    
    private boolean check(String property, String value) {
        return value.equals(SpringUtil.getProperty(property));
    }
    
    @Override
    protected List<String> getAuthorities(IDomainObject user) {
        return user == null ? null : StrUtil.toList(mockAuthorities, ",");
    }
    
    public void setMockAuthorities(String mockAuthorities) {
        this.mockAuthorities = mockAuthorities;
    }
    
    public void setMockUserFactory(IMockUserFactory<?> mockUserFactory) {
        this.mockUserFactory = mockUserFactory;
    }
    
}
