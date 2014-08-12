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

import org.carewebframework.api.domain.IUser;

/**
 * Mock user for testing.
 */
public class MockUser implements IUser {
    
    private final String logicalId;
    
    private final String fullName;
    
    private final String domainName;
    
    public MockUser(String logicalId, String fullName, String domainName) {
        this.logicalId = logicalId;
        this.fullName = fullName;
        this.domainName = domainName;
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
    public String getDomainName() {
        return domainName;
    }
    
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    @Override
    public MockUser getNativeUser() {
        return this;
    }
    
}
