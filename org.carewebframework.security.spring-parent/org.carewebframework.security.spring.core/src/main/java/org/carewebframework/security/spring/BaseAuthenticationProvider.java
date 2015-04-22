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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.SecurityDomainRegistry;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Provides authentication support for the framework. Takes provided authentication credentials and
 * authenticates them against the database.
 */
public class BaseAuthenticationProvider implements AuthenticationProvider {
    
    private static final Log log = LogFactory.getLog(BaseAuthenticationProvider.class);
    
    private final List<String> systemGrantedAuthorities = new ArrayList<String>();
    
    public BaseAuthenticationProvider() {
        this(false);
    }
    
    public BaseAuthenticationProvider(boolean debugRole) {
        systemGrantedAuthorities.add(Constants.ROLE_USER);
        
        if (debugRole) {
            systemGrantedAuthorities.add(Constants.PRIV_DEBUG);
        }
    }
    
    public BaseAuthenticationProvider(List<String> grantedAuthorities) {
        this(false);
        this.systemGrantedAuthorities.addAll(grantedAuthorities);
    }
    
    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
    /**
     * Authentication Provider. Produces a trusted <code>UsernamePasswordAuthenticationToken</code>
     * if
     * 
     * @param authentication The authentication context.
     * @return authentication Authentication object if authentication succeeded. Null if not.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CWFAuthenticationDetails details = (CWFAuthenticationDetails) authentication.getDetails();
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        String domain = null;
        
        if (log.isDebugEnabled()) {
            log.debug("User: " + username);
            log.debug("Details, RA: " + details == null ? "null" : details.getRemoteAddress());
        }
        
        if (username != null && username.contains("\\")) {
            String pcs[] = username.split("\\\\", 2);
            domain = pcs[0];
            username = pcs.length > 1 ? pcs[1] : null;
        }
        
        ISecurityDomain securityDomain = SecurityDomainRegistry.getSecurityDomain(domain);
        
        if (username == null || password == null || securityDomain == null) {
            throw new BadCredentialsException("Missing security credentials.");
        }
        
        IUser user = securityDomain.authenticate(username, password);
        details.setDetail("user", user);
        Set<String> mergedAuthorities = mergeAuthorities(securityDomain.getGrantedAuthorities(user),
            systemGrantedAuthorities);
        List<GrantedAuthority> userAuthorities = new ArrayList<GrantedAuthority>();
        
        for (String authority : mergedAuthorities) {
            if (!authority.isEmpty()) {
                userAuthorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        
        User principal = new User(username, password, true, true, true, true, userAuthorities);
        authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
                principal.getAuthorities());
        ((UsernamePasswordAuthenticationToken) authentication).setDetails(details);
        return authentication;
    }
    
    /**
     * Merges authorities granted (or revoked) by the system with those granted by the security
     * domain.
     * 
     * @param userGrants Authorities granted by the security domain.
     * @param systemGrants Authorities granted by the system.
     * @return A merged set of granted authorities.
     */
    protected Set<String> mergeAuthorities(List<String> userGrants, List<String> systemGrants) {
        Set<String> authorities = userGrants == null ? new HashSet<String>() : new HashSet<String>(userGrants);
        
        for (String grantedAuthority : systemGrantedAuthorities) {
            if (grantedAuthority.startsWith("-")) {
                authorities.remove(grantedAuthority.substring(1));
            } else {
                authorities.add(grantedAuthority);
            }
        }
        
        return authorities;
    }
    
}
