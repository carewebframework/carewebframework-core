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

import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.ui.spring.AbstractScope;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Implements a custom Spring scope based on Spring Security authentication.
 */
public class AuthenticationScope extends AbstractScope<Authentication> {
    
    @Override
    protected ScopeContainer getScopeContainer(Authentication scope) {
        CWFAuthenticationDetails details = scope == null ? null : (CWFAuthenticationDetails) scope.getDetails();
        return details == null ? null : (ScopeContainer) details.getDetail(getKey());
    }
    
    @Override
    protected void bindContainer(Authentication scope, ScopeContainer container) {
        CWFAuthenticationDetails details = (CWFAuthenticationDetails) scope.getDetails();
        details.setDetail(getKey(), container);
    }
    
    @Override
    protected Authentication getActiveScope() {
        Authentication scope = AbstractSecurityService.getAuthentication();
        return scope instanceof AnonymousAuthenticationToken ? null : scope;
    }
    
}
