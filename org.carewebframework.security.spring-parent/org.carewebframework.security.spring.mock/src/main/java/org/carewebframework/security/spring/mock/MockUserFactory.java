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

import java.io.Serializable;

import org.carewebframework.api.domain.IDomainObject;
import org.carewebframework.security.spring.mock.MockAuthenticationProvider.IMockUserFactory;

/**
 * Default factory for creating a mock user.
 */
public class MockUserFactory implements IMockUserFactory {
    
    private static class MockUser implements IDomainObject, Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private String id = "123";
        
        private final String fullName;
        
        private MockUser(String fullName) {
            this.fullName = fullName;
        }
        
        @Override
        public String getDomainId() {
            return id;
        }
        
        @Override
        public void setDomainId(String id) {
            this.id = id;
        }
        
        @Override
        public Object getProxiedObject() {
            return this;
        }
        
        @Override
        public String toString() {
            return fullName;
        }
    }
    
    @Override
    public IDomainObject create(String name) {
        return new MockUser(name);
    }
    
}
