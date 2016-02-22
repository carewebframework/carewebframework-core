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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.security.SecurityDomainRegistry;

/**
 * Loader for mock security domains.
 */
public class MockSecurityDomainLoader {
    
    /**
     * Set the mock security domains. The format is a comma-separated list of mock domains where
     * each entry is of the form: [logicalId]^[name]^[key=value]...
     * 
     * @param domains Mock security domains.
     * @param authorities Comma-separated list of granted authorities.
     */
    public MockSecurityDomainLoader(String domains, String authorities) {
        for (String domain : domains.split("\\,")) {
            String[] pcs = domain.split("\\^");
            Map<String, String> attrs = new HashMap<>();
            
            for (int i = 2; i < pcs.length; i++) {
                String[] nv = pcs[i].split("\\=", 2);
                
                if (nv.length == 2) {
                    attrs.put(nv[0], nv[1]);
                }
            }
            
            MockSecurityDomain securityDomain = new MockSecurityDomain(pcs[0], pcs[1], attrs);
            securityDomain.setMockAuthorities(authorities);
            SecurityDomainRegistry.registerSecurityDomain(securityDomain);
        }
    }
}
