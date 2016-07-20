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
package org.carewebframework.api.security;

import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;

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
     * Register an alias for an authority.
     * 
     * @param authority String representation of an authority.
     * @param alias String representation of an authority alias. If null, removes an existing alias.
     */
    public static void setAuthorityAlias(String authority, String alias) {
        getSecurityService().setAuthorityAlias(authority, alias);
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
     * @param authority String representation of an authority
     * @return boolean true if found
     * @see ISecurityService#isGranted(String)
     */
    public static boolean isGranted(String authority) {
        return getSecurityService().isGranted(authority);
    }
    
    /**
     * Determines whether the authenticated user has any of the specified granted authorities
     * 
     * @param authorities Comma-delimited string of granted authorities
     * @return boolean true only if user has 1 or more of the specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isGrantedAny(String authorities) {
        return getSecurityService().isGranted(authorities, false);
    }
    
    /**
     * Determines whether the authenticated user contains NONE of the specified granted authorities
     * 
     * @param authorities Comma-delimited string of granted authorities
     * @return boolean true only if user has NONE of the specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isGrantedNone(String authorities) {
        return !getSecurityService().isGranted(authorities, false);
    }
    
    /**
     * Determines if a user has all specified granted authorities
     * 
     * @param authorities Comma-delimited string of granted authorities
     * @return boolean true only if user has ALL specified granted authorities
     * @see ISecurityService#isGranted(String, boolean)
     */
    public static boolean isGrantedAll(String authorities) {
        return getSecurityService().isGranted(authorities, true);
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
     * Generates a random password within the specified parameters.
     * 
     * @param minLength Minimum password length.
     * @param maxLength Maximum password length.
     * @param constraints Password constraints. This is an array of character groups, each of the
     *            format <br>
     *            <code>[minimum required occurrences],[string of characters in group]</code>
     * @return A random password.
     */
    public static String generateRandomPassword(int minLength, int maxLength, String[] constraints) {
        if (constraints == null || constraints.length == 0 || minLength <= 0 || maxLength < minLength) {
            throw new IllegalArgumentException();
        }
        
        int pwdLength = RandomUtils.nextInt(maxLength - minLength + 1) + minLength;
        int[] min = new int[constraints.length];
        String[] chars = new String[constraints.length];
        char[] pwd = new char[pwdLength];
        int totalRequired = 0;
        
        for (int i = 0; i < constraints.length; i++) {
            String[] pcs = constraints[i].split("\\,", 2);
            min[i] = Integer.parseInt(pcs[0]);
            chars[i] = pcs[1];
            totalRequired += min[i];
        }
        
        if (totalRequired > maxLength) {
            throw new IllegalArgumentException("Maximum length and constraints in conflict.");
        }
        
        int grp = 0;
        
        while (pwdLength-- > 0) {
            if (min[grp] <= 0) {
                grp = totalRequired > 0 ? grp + 1 : RandomUtils.nextInt(constraints.length);
            }
            
            int i = RandomUtils.nextInt(pwd.length);
            
            while (pwd[i] != 0) {
                i = ++i % pwd.length;
            }
            
            pwd[i] = chars[grp].charAt(RandomUtils.nextInt(chars[grp].length()));
            min[grp]--;
            totalRequired--;
        }
        
        return new String(pwd);
    }
    
    /**
     * Enforce static class.
     */
    private SecurityUtil() {
    };
}
