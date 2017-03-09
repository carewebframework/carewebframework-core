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
package org.carewebframework.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.alias.AliasType;
import org.carewebframework.api.alias.AliasTypeRegistry;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.context.IContextManager;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.controller.PasswordChangeController;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.core.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.ObjectUtils;

/**
 * Base Spring Security implementation.
 */
public abstract class AbstractSecurityService implements ISecurityService {

    private static final Log log = LogFactory.getLog(AbstractSecurityService.class);

    private String logoutTarget;

    private final AliasType authorityAlias = AliasTypeRegistry.getType(ALIAS_TYPE_AUTHORITY);

    public static SecurityContext getSecurityContext() {
        return (SecurityContext) ExecutionContext.getSession().getSocket().getAttributes()
                .get(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    }
    
    /**
     * Returns Spring security Authentication object via
     * <code>SpringContextHolder.getContext().getAuthentication()</code>.
     *
     * @return Authentication or null if no authentication information is found
     */
    public static Authentication getAuthentication() {
        return getSecurityContext().getAuthentication();
    }

    /**
     * Gets the specified logout attribute value. The value is obtained from a cookie which is then
     * deleted.
     *
     * @param attributeName Name of the logout attribute.
     * @param deflt Default value to return if none found.
     * @param response The response.
     * @param request The request.
     * @return Value of the attribute, which is automatically converted from its base64 encoding.
     */
    public static String getLogoutAttribute(String attributeName, String deflt, HttpServletResponse response,
                                            HttpServletRequest request) {
        String value = WebUtil.getCookieValue(attributeName, request);
        //delete cookie
        WebUtil.setCookie(attributeName, null, response, request);
        return StringUtils.isEmpty(value) ? deflt : value;
    }

    public String getLogoutUrl(String target, String message) {
        target = target == null ? WebUtil.getRequestUrl() : target;
        String url = replaceParam(logoutTarget, "%target%", target);
        return replaceParam(url, "%message%", message);
    }

    /**
     * Logout out the current page instance.
     *
     * @param force If true, force logout without user interaction.
     * @param target Optional target url for next login.
     * @param message Optional message to indicate reason for logout.
     */
    @Override
    public void logout(boolean force, String target, String message) {
        log.trace("Logging Out");
        IContextManager contextManager = ContextManager.getInstance();

        if (contextManager == null) {
            logout(target, message);
        } else {
            contextManager.reset(force, (response) -> {
                if (force || !response.rejected()) {
                    logout(target, message);
                }
            });
        }
    }

    private void logout(String target, String message) {
        ClientUtil.redirect(getLogoutUrl(target, message));
    }

    /**
     * Replaces the inline parameter with the specified value.
     *
     * @param text Text containing parameter placeholder.
     * @param param Parameter name.
     * @param value Value to replace (will be url-encoded).
     * @return Updated text.
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
        authorityAlias.register(authority, alias);
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
        return (details instanceof CWFAuthenticationDetails) ? (IUser) ((CWFAuthenticationDetails) details).getDetail("user")
                : null;
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
     * @param grantedAuthority The granted authority to check.
     * @param authentication The authentication context.
     * @return True if the granted authority exists within the authentication context.
     */
    private boolean isGranted(String grantedAuthority, Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication context was null during check for granted authority '" + grantedAuthority + "'.");
            return false;
        }

        boolean result = authentication.getAuthorities().contains(new SimpleGrantedAuthority(grantedAuthority));

        if (!result) {
            String alias = authorityAlias.get(grantedAuthority);
            return alias != null && isGranted(alias, authentication);
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
        this.logoutTarget = logoutTarget;
    }

    /**
     * @see org.carewebframework.api.security.ISecurityService#changePassword()
     */
    @Override
    public void changePassword() {
        if (canChangePassword()) {
            PasswordChangeController.show();
        } else {
            DialogUtil.showWarning(StrUtil.getLabel(Constants.LBL_PASSWORD_CHANGE_UNAVAILABLE));
        }
    }

    /**
     * @see org.carewebframework.api.security.ISecurityService#canChangePassword()
     */
    @Override
    public boolean canChangePassword() {
        return true;
    }

    /**
     * Generates a new random password Length of password dictated by
     * {@link Constants#LBL_PASSWORD_RANDOM_LENGTH} and
     * {@link Constants#LBL_PASSWORD_RANDOM_CONSTRAINTS}
     *
     * @return String The generated password
     */
    @Override
    public String generateRandomPassword() {
        int len = getRandomPasswordLength();
        return SecurityUtil.generateRandomPassword(len, len,
            StrUtil.getLabel(Constants.LBL_PASSWORD_RANDOM_CONSTRAINTS).split("\n"));
    }

    /**
     * Returns the minimum length for random password.
     *
     * @return Minimum length for random password.
     */
    protected int getRandomPasswordLength() {
        return NumberUtils.toInt(StrUtil.getLabel(Constants.LBL_PASSWORD_RANDOM_LENGTH), 12);
    }

}
