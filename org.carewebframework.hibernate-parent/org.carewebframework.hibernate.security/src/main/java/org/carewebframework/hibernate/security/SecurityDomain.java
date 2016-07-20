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

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.spring.SpringUtil;

import org.springframework.security.authentication.BadCredentialsException;

@Entity
@Table(name = "CWF_DOMAIN")
@EntityListeners({ SecurityDomain.class })
public class SecurityDomain implements ISecurityDomain {
    
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private String id;
    
    private String name;
    
    @Lob
    private String attributes;
    
    @Transient
    private Properties properties;
    
    public SecurityDomain() {
    }
    
    public SecurityDomain(String id, String name, String attributes) {
        this.id = id;
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
        return id;
    }
    
    @Override
    public String getAttribute(String name) {
        initProperties();
        return properties.getProperty(name);
    }
    
    @Override
    public IUser authenticate(String username, String password) {
        IUser user = getUserDAO().authenticate(username, password, this);
        
        if (user == null) {
            throw new BadCredentialsException("Incorrect username or password.");
        }
        
        return user;
    }
    
    @Override
    public SecurityDomain getNativeSecurityDomain() {
        return this;
    }
    
    @Override
    public List<String> getGrantedAuthorities(IUser user) {
        return ((User) user).getGrantedAuthorities();
    }
    
    private void initProperties() {
        if (properties == null) {
            properties = new Properties();
            
            if (attributes != null) {
                try (StringReader reader = new StringReader(attributes.replace(';', '\n'));) {
                    this.properties.load(reader);
                } catch (Exception e) {
                    
                }
            }
        }
    }
    
    private UserDAO getUserDAO() {
        return SpringUtil.getAppContext().getBean(UserDAO.class);
    }
}
