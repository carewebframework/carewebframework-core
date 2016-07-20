/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
