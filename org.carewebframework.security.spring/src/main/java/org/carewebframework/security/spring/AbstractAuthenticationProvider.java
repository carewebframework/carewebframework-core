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

import org.carewebframework.api.domain.IDomainObject;

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
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {
    
    private static final Log log = LogFactory.getLog(AbstractAuthenticationProvider.class);
    
    protected boolean preAuthenticated;
    
    private final List<String> grantedRoles = new ArrayList<String>();
    
    protected AbstractAuthenticationProvider(boolean debugRole) {
        grantedRoles.add(Constants.ROLE_USER);
        
        if (debugRole) {
            grantedRoles.add(Constants.PRIV_DEBUG);
        }
    }
    
    protected AbstractAuthenticationProvider(List<String> grantedRoles) {
        this(false);
        this.grantedRoles.addAll(grantedRoles);
    }
    
    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
    /**
     * Authentication Provider. Produces a trusted <code>UsernamePasswordAuthenticationToken</code>
     * if
     * 
     * @param authentication
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
        
        if (username != null) {
            String pcs[] = username.split("\\\\", 2);
            domain = pcs[0];
            username = pcs.length > 1 ? pcs[1] : null;
        }
        
        if (username == null || password == null || domain == null) {
            throw new BadCredentialsException("Missing security credentials.");
        }
        
        IDomainObject user = login(details, username, password, domain);
        List<GrantedAuthority> userPrivs = new ArrayList<GrantedAuthority>();
        List<String> list = getAuthorities(user);
        Set<String> privs = list == null ? new HashSet<String>() : new HashSet<String>(list);
        
        for (String grantedRole : grantedRoles) {
            if (grantedRole.startsWith("-")) {
                privs.remove(grantedRole.substring(1));
            } else {
                privs.add(grantedRole);
            }
        }
        
        for (String priv : privs) {
            if (!priv.isEmpty()) {
                userPrivs.add(new SimpleGrantedAuthority(priv));
            }
        }
        
        User principal = new User(username, password, true, true, true, true, userPrivs);
        
        authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
                principal.getAuthorities());
        ((UsernamePasswordAuthenticationToken) authentication).setDetails(details);
        return authentication;
    }
    
    protected abstract List<String> getAuthorities(IDomainObject user);
    
    /**
     * Performs a user login.
     * 
     * @param details Authentication details
     * @param username Username for the login.
     * @param password Password for the login (ignored if the user is pre-authenticated).
     * @param domain Domain for which the login is requested.
     * @return User object
     */
    protected abstract IDomainObject login(CWFAuthenticationDetails details, String username, String password, String domain);
    
}
