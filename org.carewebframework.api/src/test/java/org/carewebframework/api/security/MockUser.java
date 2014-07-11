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

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.api.domain.IUser;

/**
 * Mock user for testing.
 */
public class MockUser extends DomainObject implements IUser {
    
    private static final long serialVersionUID = 1L;
    
    private String username;
    
    private final String fullName;
    
    public MockUser(String id, String username, String fullName) {
        super(id);
        this.username = username;
        this.fullName = fullName;
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
    public String getFullName() {
        return fullName;
    }
    
    @Override
    public IInstitution getInstitution() {
        return null;
    }
    
}
