/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring;

import org.carewebframework.api.spring.AbstractScope;
import org.carewebframework.api.spring.ScopeContainer;

import org.springframework.security.core.Authentication;

/**
 * Implements a custom Spring scope based on Spring Security authentication.
 */
public class AuthenticationScope extends AbstractScope {
    
    private static final String KEY_SCOPE = "scope_container";
    
    @Override
    protected ScopeContainer getContainer() {
        Authentication auth = AbstractSecurityService.getAuthentication();
        CWFAuthenticationDetails details = auth == null ? null : (CWFAuthenticationDetails) auth.getDetails();
        ScopeContainer container = null;
        
        if (details != null) {
            container = (ScopeContainer) details.getDetail(KEY_SCOPE);
            
            if (container == null) {
                container = new ScopeContainer();
                details.setDetail(KEY_SCOPE, container);
            }
        }
        
        return container;
    }
    
}
