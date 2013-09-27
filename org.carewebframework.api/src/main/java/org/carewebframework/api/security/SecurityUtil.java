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

import java.util.Map;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.spring.SpringUtil;

import org.springframework.util.AntPathMatcher;

/**
 * Utility class for security-related functions.
 */
public class SecurityUtil {
    
    private static final AntPathMatcher urlMatcher = new AntPathMatcher();
    
    private static ISecurityService securityService;
    
    /**
     * Returns a reference to the security service.
     * 
     * @return The security service instance.
     */
    public static ISecurityService getSecurityService() {
        if (securityService == null) {
            securityService = SpringUtil.getBean("securityService", ISecurityService.class);
        }
        
        return securityService;
    }
    
    /**
     * Returns whether the current context has authenticated
     * 
     * @return boolean true if Authentication token is found and is not an Anonymous User
     * @see ISecurityService#isAuthenticated()
     */
    public static boolean isAuthenticated() {
        return getSecurityService().isAuthenticated();
    }
    
    /**
     * Returns the currently authenticated user.
     * 
     * @return The authenticated user, or null if none.
     * @see ISecurityService#getAuthenticatedUser()
     */
    public static IUser getAuthenticatedUser() {
        return getSecurityService().getAuthenticatedUser();
    }
    
    /**
     * Returns the currently authenticated user's username.
     * 
     * @return The authenticated username, or null if none.
     */
    public static String getAuthenticatedUsername() {
        IUser user = getSecurityService().getAuthenticatedUser();
        return user == null ? null : user.getUsername();
    }
    
    /**
     * Returns true if the Authentication object is granted debug privilege.
     * 
     * @return True if authenticated principal is granted a debug privilege.
     * @see ISecurityService#hasDebugRole()
     */
    public static boolean hasDebugRole() {
        return getSecurityService().hasDebugRole();
    }
    
    /**
     * <p>
     * Returns true if the Authentication object has the specified <code>grantedAuthority</code>
     * </p>
     * <p>
     * <i>Note:</i>Privileges are prefixed with "PRIV_" and roles are prefixed with "ROLE_"
     * </p>
     * 
     * @param grantedAuthority String representation of an authority
     * @return boolean true if found
     * @see ISecurityService#isGranted(String)
     */
    public static boolean isGranted(String grantedAuthority) {
        return getSecurityService().isGranted(grantedAuthority);
    }
    
    /**
     * Determines whether the authenticated user has any of the specified granted authorities
     * 
     * @param roles Comma-delimited string of granted authorities
     * @return boolean true only if user has 1 or more of the specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isUserInAnyRoles(String roles) {
        return getSecurityService().isGranted(roles, false);
    }
    
    /**
     * Determines whether the authenticated user contains NONE of the specified granted authorities
     * 
     * @param roles Comma-delimited string of granted authorities
     * @return boolean true only if user has NONE of the specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isUserNotInAllRoles(String roles) {
        return !getSecurityService().isGranted(roles, false);
    }
    
    /**
     * Determines if a user has all specified granted authorities
     * 
     * @param roles Comma-delimited string of granted authorities
     * @return boolean true only if user has ALL specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isUserInAllRoles(String roles) {
        return getSecurityService().isGranted(roles, true);
    }
    
    /**
     * Returns the target of a url pattern mapping.
     * 
     * @param url Ant-style url pattern
     * @param urlMappings Map of url pattern mappings
     * @return String mapped to pattern, or null if none.
     */
    public static String getUrlMapping(String url, Map<String, String> urlMappings) {
        if (urlMappings != null) {
            for (String pattern : urlMappings.keySet()) {
                if (urlMatcher.match(pattern, url)) {
                    return urlMappings.get(pattern);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Enforce static class.
     */
    private SecurityUtil() {
    };
}
