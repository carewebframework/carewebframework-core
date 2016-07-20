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
import javax.persistence.Transient;

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
    private SecurityDomain assignedDomain;
    
    @Column(name = "name")
    private String fullName;
    
    @Column(name = "username")
    private String loginName;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "authorities")
    @Lob
    private String authorities;
    
    @Transient
    private SecurityDomain loginDomain;
    
    protected User() {
    }
    
    public User(String logicalId, String fullName, String loginName, String password, SecurityDomain securityDomain,
        String authorities) {
        this.logicalId = logicalId;
        this.fullName = fullName;
        this.loginName = loginName;
        this.password = password;
        this.assignedDomain = securityDomain;
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
        return loginDomain != null ? loginDomain : assignedDomain;
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
    
    protected void setLoginDomain(SecurityDomain loginDomain) {
        this.loginDomain = loginDomain;
    }
    
    public List<String> getGrantedAuthorities() {
        return StrUtil.toList(authorities, ",");
    }
}
