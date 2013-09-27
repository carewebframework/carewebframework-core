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
     * Returns an instance of a CWAuthenticationDetails object.
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
