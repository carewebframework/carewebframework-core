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
        return getUserDAO().authenticate(username, password, this);
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
