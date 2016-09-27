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
package org.carewebframework.security.spring;

import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.web.spring.AbstractScope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Implements a custom Spring scope tied to Spring Security authentication.
 */
public class AuthenticationScope extends AbstractScope<Authentication> {
    
    public AuthenticationScope() {
        super(true);
    }
    
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
