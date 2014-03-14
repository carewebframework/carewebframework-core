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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.AliasRegistry;
import org.carewebframework.api.AliasRegistry.AliasRegistryForType;
import org.carewebframework.api.AliasRegistry.AliasType;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.context.IContextManager;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.ui.Application;
import org.carewebframework.ui.FrameworkWebSupport;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import org.zkoss.zk.ui.Desktop;

/**
 * Base Spring Security implementation.
 */
public abstract class AbstractSecurityService implements ISecurityService {
    
    private static final Log log = LogFactory.getLog(AbstractSecurityService.class);
    
    protected final AliasRegistryForType authorityAliases = AliasRegistry.getInstance(AliasType.AUTHORITY);
    
    private String logoutTarget = Constants.LOGOUT_TARGET;
    
    /**
     * Returns Spring security Authentication object via
     * <code>SpringContextHolder.getContext().getAuthentication()</code>.
     * 
     * @return Authentication or null if no authentication information is found
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Sets cookies for logout.
     * 
     * @param target Target url after successful login.
     * @param message Message to display in logout dialog.
     */
    protected static void setLogoutAttributes(final String target, final String message) {
        setCookie(Constants.LOGOUT_WARNING_ATTR, message, "Application logged out.");
        setCookie(Constants.LOGOUT_TARGET_ATTR, target, "/");
    }
    
    /**
     * Sets the cookie in the http response.
     * 
     * @param cookieName Name of the cookie.
     * @param value Value of the cookie which will be base64-encoded.
     * @param deflt Default value if value is null.
     */
    private static void setCookie(final String cookieName, final String value, final String deflt) {
        FrameworkWebSupport.setCookie(cookieName, (value == null ? deflt : value));
    }
    
    /**
     * Gets the specified logout attribute value. The value is obtained from a cookie which is then
     * deleted.
     * 
     * @param attributeName Name of the logout attribute.
     * @param deflt Default value to return if none found.
     * @return Value of the attribute, which is automatically converted from its base64 encoding.
     */
    protected static String getLogoutAttribute(final String attributeName, final String deflt) {
        final String value = FrameworkWebSupport.getCookieValue(attributeName);
        //delete cookie
        FrameworkWebSupport.setCookie(attributeName, null);
        return StringUtils.isEmpty(value) ? deflt : value;
    }
    
    /**
     * Logout out the current desktop instance.
     * 
     * @param force If true, force logout without user interaction.
     * @param target Optional target url for next login.
     * @param message Optional message to indicate reason for logout.
     * @return True if operation was successful.
     */
    @Override
    public boolean logout(final boolean force, String target, final String message) {
        log.trace("Logging Out");
        final IContextManager contextManager = ContextManager.getInstance();
        final boolean result = contextManager == null || contextManager.reset(force) || force;
        
        if (result) {
            if (target == null) {
                try {
                    target = FrameworkWebSupport.addQueryString(FrameworkWebSupport.getRequestUrl(),
                        FrameworkWebSupport.getRequestParams());
                } catch (final Exception e) {}
            }
            
            AbstractSecurityService.setLogoutAttributes(target, message);
            final Desktop contextDesktop = FrameworkWebSupport.getDesktop();
            log.debug("Redirecting Desktop to logout filter URI: " + contextDesktop);
            String queryParam = replaceParam(replaceParam(logoutTarget, "%target%", target), "%message%", message);
            contextDesktop.getExecution().sendRedirect(Constants.LOGOUT_URI + queryParam);
            Application.getInstance().register(contextDesktop, false);
        }
        
        return result;
    }
    
    /**
     * Replaces the inline parameter with the specified value.
     * 
     * @param text Text containing parameter placeholder.
     * @param param Parameter name.
     * @param value Value to replace (will be url-encoded).
     * @return
     */
    private String replaceParam(String text, String param, String value) {
        if (text.contains(param)) {
            try {
                value = value == null ? "" : URLEncoder.encode(value, CharEncoding.UTF_8);
                text = text.replace(param, value);
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding parameter value.", e);
            }
        }
        return text;
    }
    
    /**
     * Register an alias for an authority.
     * 
     * @param authority String representation of an authority.
     * @param alias String representation of an authority alias. If null, removes an existing alias.
     */
    @Override
    public void setAuthorityAlias(String authority, String alias) {
        authorityAliases.registerAlias(authority, alias);
    }
    
    /**
     * Returns whether the current context has authenticated
     * 
     * @return boolean true if Authentication token is found and is not an Anonymous User
     */
    @Override
    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        
        if (auth == null) {
            return false;
        }
        
        Object principal = auth.getPrincipal();
        String username = principal instanceof String ? (String) principal
                : ((org.springframework.security.core.userdetails.User) principal).getUsername();
        return (username != null && !username.equals(Constants.ANONYMOUS_USER));
    }
    
    /**
     * Returns the authenticated user object from the current security context.
     * 
     * @return The authenticated user object, or null if none present.
     */
    @Override
    public IUser getAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        Object details = authentication == null ? null : authentication.getDetails();
        return (details instanceof CWFAuthenticationDetails) ? (IUser) ((CWFAuthenticationDetails) details)
                .getDetail("user") : null;
    }
    
    /**
     * <p>
     * Returns true if the Authentication object is granted debug privilege (determined by the role
     * {@link Constants#PRIV_DEBUG})
     * </p>
     * 
     * @return boolean true if authenticated principal is granted a verbose view
     */
    @Override
    public boolean hasDebugRole() {
        return isGranted(Constants.PRIV_DEBUG);
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
     */
    @Override
    public boolean isGranted(String grantedAuthority) {
        return isGranted(grantedAuthority, getAuthentication());
    }
    
    /**
     * Checks the current SecurityContext for the specified authorities.
     * 
     * @param grantedAuthorities Comma-delimited string of granted authorities
     * @param checkAllRoles boolean true-specified roles must be found in security context
     *            authorities, false-security context must contain at least 1 specified authority
     * @return True if Authentication is granted authorities
     */
    @Override
    public boolean isGranted(String grantedAuthorities, boolean checkAllRoles) {
        Authentication authentication = getAuthentication();
        
        if (authentication == null) {
            log.info("Authentication context was null during check for granted authorities '"
                    + ObjectUtils.nullSafeToString(grantedAuthorities) + "'.");
            return false;
        }
        
        if (grantedAuthorities == null) {
            return false;
        }
        
        for (String desiredAuthority : grantedAuthorities.split(",")) {
            if (!desiredAuthority.isEmpty()) {
                if (isGranted(desiredAuthority, authentication) != checkAllRoles) {
                    return !checkAllRoles;
                }
            }
        }
        
        return checkAllRoles;
    }
    
    /**
     * Determine if the granted authority exists within the authentication context.
     * 
     * @param grantedAuthority
     * @param authentication
     * @return
     */
    private boolean isGranted(String grantedAuthority, Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication context was null during check for granted authority '" + grantedAuthority + "'.");
            return false;
        }
        
        boolean result = authentication.getAuthorities().contains(new SimpleGrantedAuthority(grantedAuthority));
        
        if (!result && authorityAliases.contains(grantedAuthority)) {
            return isGranted(authorityAliases.get(grantedAuthority), authentication);
        }
        
        return result;
    }
    
    /**
     * Override to implement login restrictions.
     */
    @Override
    public String loginDisabled() {
        return null;
    }
    
    /**
     * Returns the logout target url.
     * 
     * @return Logout target url.
     */
    public String getLogoutTarget() {
        return logoutTarget;
    }
    
    /**
     * Sets the logout target url.
     * 
     * @param logoutTarget Logout target url.
     */
    public void setLogoutTarget(String logoutTarget) {
        this.logoutTarget = StringUtils.isEmpty(logoutTarget) ? Constants.LOGOUT_TARGET : logoutTarget;
    }
    
}
