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
import org.carewebframework.api.domain.IDomainObject;

/**
 * Mock user for testing.
 */
public class MockUser extends DomainObject implements IDomainObject {
    
    private static final long serialVersionUID = 1L;
    
    private final String fullName;
    
    public MockUser(String id, String fullName) {
        super(id);
        this.fullName = fullName;
    }
    
    @Override
    public String toString() {
        return fullName;
    }
    
}
