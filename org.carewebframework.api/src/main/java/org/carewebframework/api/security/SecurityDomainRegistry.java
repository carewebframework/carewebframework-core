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

import org.carewebframework.common.AbstractRegistry;

import org.springframework.util.StringUtils;

/**
 * Tracks all security domains.
 */
public class SecurityDomainRegistry extends AbstractRegistry<String, ISecurityDomain> {
    
    private static SecurityDomainRegistry instance = new SecurityDomainRegistry();
    
    public static SecurityDomainRegistry getInstance() {
        return instance;
    }
    
    public static ISecurityDomain getSecurityDomain(String name) {
        return StringUtils.isEmpty(name) ? null : instance.get(name);
    }
    
    public static void registerSecurityDomain(ISecurityDomain securityDomain) {
        instance.register(securityDomain);
    }
    
    private SecurityDomainRegistry() {
        super();
    }
    
    @Override
    protected String getKey(ISecurityDomain item) {
        return item.getLogicalId();
    }
    
}
