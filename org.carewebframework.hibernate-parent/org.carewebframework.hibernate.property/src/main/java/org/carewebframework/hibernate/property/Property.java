/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.property;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.carewebframework.api.domain.IUser;

@Entity
@Table(name = "CWF_PROPERTY")
@IdClass(Property.PropertyId.class)
public class Property implements Serializable {
    
    
    public static class PropertyId implements Serializable {
        
        
        private static final long serialVersionUID = 1L;
        
        protected String name;
        
        protected String instance;
        
        protected String user;
        
        public PropertyId() {
            
        }
        
        public PropertyId(String name, String instance, String user) {
            this.name = name;
            this.instance = instance == null ? "" : instance;
            this.user = user == null ? "" : user;
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private String name;
    
    @Id
    private String instance;
    
    @Id
    private String user;
    
    @Lob
    private String value;
    
    public Property() {
    }
    
    public Property(String name) {
        this(name, null, null);
    }
    
    public Property(String name, String value) {
        this(name, value, null);
    }
    
    public Property(String name, String value, String instance) {
        this(name, value, instance, null);
    }
    
    public Property(String name, String value, String instance, IUser user) {
        setName(name);
        setValue(value);
        setInstance(instance);
        setUser(user == null ? null : user.getLogicalId());
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getInstance() {
        return instance;
    }
    
    public void setInstance(String instance) {
        this.instance = instance == null ? "" : instance;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user == null ? "" : user;
    }
}
