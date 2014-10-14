/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.security.mock;

import java.util.Collections;
import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.ISecurityService;

/**
 * Mock security service for testing.
 */
public class MockSecurityService implements ISecurityService {
    
    private final IUser mockUser;
    
    public MockSecurityService(IUser mockUser) {
        this.mockUser = mockUser;
    }
    
    @Override
    public boolean logout(boolean force, String target, String message) {
        return true;
    }
    
    @Override
    public boolean validatePassword(String password) {
        return true;
    }
    
    @Override
    public String changePassword(String oldPassword, String newPassword) {
        return null;
    }
    
    @Override
    public void changePassword() {
    }
    
    @Override
    public boolean canChangePassword() {
        return false;
    }
    
    @Override
    public String generateRandomPassword() {
        return null;
    }
    
    @Override
    public void setAuthorityAlias(String authority, String alias) {
    }
    
    @Override
    public boolean isAuthenticated() {
        return true;
    }
    
    @Override
    public IUser getAuthenticatedUser() {
        return mockUser;
    }
    
    @Override
    public boolean hasDebugRole() {
        return true;
    }
    
    @Override
    public boolean isGranted(String grantedAuthority) {
        return true;
    }
    
    @Override
    public boolean isGranted(String grantedAuthorities, boolean checkAllRoles) {
        return true;
    }
    
    @Override
    public String loginDisabled() {
        return null;
    }
    
    @Override
    public List<ISecurityDomain> getSecurityDomains() {
        ISecurityDomain securityDomain = mockUser == null ? null : mockUser.getSecurityDomain();
        
        if (securityDomain == null) {
            return Collections.emptyList();
        }
        
        return Collections.singletonList(securityDomain);
    }
    
}
