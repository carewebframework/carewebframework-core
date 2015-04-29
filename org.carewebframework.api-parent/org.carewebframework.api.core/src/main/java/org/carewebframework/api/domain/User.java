/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.domain;

import org.carewebframework.api.security.ISecurityDomain;

/**
 * Simple implementation of a user.
 */
public class User implements IUser {
    
    private static final long serialVersionUID = 1L;
    
    private String logicalId;
    
    private String fullName;
    
    private String loginName;
    
    private String password;
    
    private ISecurityDomain securityDomain;
    
    public User() {
        
    }
    
    public User(String logicalId, String fullName, String loginName, String password, ISecurityDomain securityDomain) {
        this.logicalId = logicalId;
        this.fullName = fullName;
        this.loginName = loginName;
        this.password = password;
        this.securityDomain = securityDomain;
    }
    
    /**
     * Returns the logical identifier for the user.
     * 
     * @return User logical identifier.
     */
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    /**
     * Sets the logical identifier for the user.
     * 
     * @param logicalId User logical identifier.
     */
    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }
    
    /**
     * Returns the user's full name.
     * 
     * @return User's full name.
     */
    @Override
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Sets the user's full name.
     * 
     * @param fullName User's full name.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    /**
     * Return the user's login name.
     * 
     * @return User's login name.
     */
    @Override
    public String getLoginName() {
        return loginName;
    }
    
    /**
     * Sets the user's login name.
     * 
     * @param loginName User's login name.
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    
    /**
     * Return the user's password.
     * 
     * @return User's password.
     */
    @Override
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the user's password.
     * 
     * @param password User's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Returns the user's security domain.
     * 
     * @return User's security domain.
     */
    @Override
    public ISecurityDomain getSecurityDomain() {
        return securityDomain;
    }
    
    /**
     * Sets the user's security domain.
     * 
     * @param securityDomain User's security domain.
     */
    public void setSecurityDomain(ISecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }
    
    /**
     * Returns the native user object if this is a proxy.
     * 
     * @return The native user object.
     */
    @Override
    public User getNativeUser() {
        return this;
    }
}
