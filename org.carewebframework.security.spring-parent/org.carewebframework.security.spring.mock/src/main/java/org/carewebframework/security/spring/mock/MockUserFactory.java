/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring.mock;

import org.carewebframework.api.domain.DomainObject;
import org.carewebframework.security.spring.mock.MockAuthenticationProvider.IMockUserFactory;
import org.carewebframework.security.spring.mock.MockUserFactory.MockUser;

/**
 * Default factory for creating a mock user.
 */
public class MockUserFactory implements IMockUserFactory<MockUser> {
    
    public static class MockUser extends DomainObject {
        
        private static final long serialVersionUID = 1L;
        
        private final String id = "123";
        
        private final String fullName;
        
        private MockUser(String fullName) {
            this.fullName = fullName;
        }
        
        @Override
        public String getLogicalId() {
            return id;
        }
        
        @Override
        public String getUniversalId() {
            return id;
        }
        
        @Override
        public String toString() {
            return fullName;
        }
        
    }
    
    @Override
    public MockUser create(String name) {
        return new MockUser(name);
    }
    
}
