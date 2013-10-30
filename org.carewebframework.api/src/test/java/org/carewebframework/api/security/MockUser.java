/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.security;

import java.util.Date;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.api.domain.EntityIdentifier;
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.domain.Name;

/**
 * Mock user for testing.
 */
public class MockUser extends DomainObject implements IUser {
    
    private static final long serialVersionUID = 1L;
    
    private String username;
    
    private final Name name;
    
    public MockUser(long id, String username, String fullname) {
        super(id);
        this.username = username;
        this.name = new Name(fullname);
    }
    
    @Override
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public Name getName() {
        return name;
    }
    
    @Override
    public String getGender() {
        return null;
    }
    
    @Override
    public Date getBirthDate() {
        return null;
    }
    
    @Override
    public Date getDeathDate() {
        return null;
    }
    
    @Override
    public IInstitution getInstitution() {
        return null;
    }
    
    @Override
    public EntityIdentifier getIdentifier(String sysId) {
        // TODO Auto-generated method stub
        return null;
    }
}
