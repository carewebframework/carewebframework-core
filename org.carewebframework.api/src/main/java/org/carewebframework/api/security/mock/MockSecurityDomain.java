/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.security.mock;

import java.util.Map;

import org.carewebframework.api.security.ISecurityDomain;

/**
 * Mock user for testing.
 */
public class MockSecurityDomain implements ISecurityDomain {
    
    private static final long serialVersionUID = 1L;
    
    private final String logicalId;
    
    private final String name;
    
    private final Map<String, String> attributes;
    
    public MockSecurityDomain(String logicalId, String name) {
        this(logicalId, name, null);
    }
    
    public MockSecurityDomain(String logicalId, String name, Map<String, String> attributes) {
        this.logicalId = logicalId;
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
        return logicalId;
    }
    
    @Override
    public String getAttribute(String name) {
        return attributes == null ? null : attributes.get(name);
    }
    
    @Override
    public MockSecurityDomain getNativeSecurityDomain() {
        return this;
    }
    
}
