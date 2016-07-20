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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.authentication.AuthenticationDetailsSource;

/**
 * Generates the details object to associate with the authentication object.
 */
public class CWFAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, CWFAuthenticationDetails> {
    
    private static final Log log = LogFactory.getLog(CWFAuthenticationDetailsSource.class);
    
    /**
     * Returns an instance of a CWFAuthenticationDetails object.
     * 
     * @param request The servlet request object.
     */
    @Override
    public CWFAuthenticationDetails buildDetails(HttpServletRequest request) {
        log.trace("Building details");
        CWFAuthenticationDetails details = new CWFAuthenticationDetails(request);
        return details;
    }
    
}
