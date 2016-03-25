/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.security;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.common.StrUtil;

@Entity
@Table(name = "CWF_USER")
public class User implements IUser {
    
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "id")
    private String logicalId;
    
    @JoinColumn(name = "domain", nullable = true)
    @ManyToOne
    @PrimaryKeyJoinColumn
    private SecurityDomain securityDomain;
    
    @Column(name = "name")
    private String fullName;
    
    @Column(name = "username")
    private String loginName;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "authorities")
    @Lob
    private String authorities;
    
    protected User() {
    }
    
    public User(String logicalId, String fullName, String loginName, String password, SecurityDomain securityDomain,
        String authorities) {
        this.logicalId = logicalId;
        this.fullName = fullName;
        this.loginName = loginName;
        this.password = password;
        this.securityDomain = securityDomain;
        this.authorities = authorities;
    }
    
    @Override
    public String toString() {
        return fullName;
    }
    
    @Override
    public String getFullName() {
        return fullName;
    }
    
    @Override
    public String getLoginName() {
        return loginName;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public ISecurityDomain getSecurityDomain() {
        return securityDomain;
    }
    
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    @Override
    public User getNativeUser() {
        return this;
    }
    
    protected void setPassword(String password) {
        this.password = password;
    }
    
    protected void setSecurityDomain(SecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }
    
    public List<String> getGrantedAuthorities() {
        return StrUtil.toList(authorities, ",");
    }
}
