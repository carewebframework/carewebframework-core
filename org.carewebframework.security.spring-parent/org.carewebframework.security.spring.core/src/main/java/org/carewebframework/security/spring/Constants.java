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

import org.carewebframework.ui.zk.ZKUtil;

public class Constants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
    // Miscellaneous security constants
    
    public static final String ROLE_PREFIX = "ROLE_";
    
    public static final String PRIV_PREFIX = "PRIV_";
    
    public static final String ROLE_USER = ROLE_PREFIX + "USER";
    
    public static final String PRIV_DEBUG = PRIV_PREFIX + "DEBUG"; // Privilege that enables debug modes.
    
    public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
    
    public static final String DEFAULT_TARGET_PARAMETER = "spring-security-redirect";
    
    public static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    
    public static final String SAVED_USER = "SPRING_SECURITY_SAVED_USER";
    
    public static final String DEFAULT_SECURITY_DOMAIN = "@defaultDomain";
    
    public static final String DEFAULT_USERNAME = "@defaultUsername";
    
    public static final String DIALOG_ACCESS_DENIED = RESOURCE_PREFIX + "accessDenied.zul";
    
    // Logout constants
    
    public static final String LOGOUT_URI = "/logout?" + DEFAULT_TARGET_PARAMETER + "=";
    
    public static final String LOGOUT_WARNING_ATTR = "logoutWarningMessage";
    
    public static final String LOGOUT_TARGET_ATTR = "logoutTargetURI";
    
    public static final String LBL_LOGOUT_MESSAGE_DEFAULT = "logout.message.default";
    
    // Login constants
    
    public static final String PROP_LOGIN_LOGO = "LOGIN.LOGO";
    
    public static final String PROP_LOGIN_HEADER = "LOGIN.HEADER";
    
    public static final String PROP_LOGIN_FOOTER = "LOGIN.FOOTER";
    
    public static final String PROP_LOGIN_INFO = "LOGIN.INFO";
    
    public static final String LBL_LOGIN_PAGE_TITLE = "login.form.panel.title";
    
    public static final String LBL_LOGIN_ERROR = "login.error";
    
    public static final String LBL_LOGIN_FORM_TIMEOUT_MESSAGE = "login.form.timeout.message";
    
    public static final String LBL_LOGIN_PROGRESS = "login.progress";
    
    public static final String LBL_LOGIN_REQUIRED_FIELDS = "login.required.fields";
    
    public static final String LBL_LOGIN_NO_VALID_DOMAINS = "login.error.no.valid.domains";
    
    public static final String LBL_LOGIN_ERROR_INVALID = "login.error.invalid";
    
    public static final String LBL_LOGIN_ERROR_EXPIRED_PASSWORD = "login.error.expired.password";
    
    public static final String LBL_LOGIN_ERROR_EXPIRED_USER = "login.error.expired.user";
    
    public static final String LBL_LOGIN_ERROR_UNEXPECTED = "login.error.unexpected";
    
    // Password constants
    
    public static final String PASSWORD_EXPIRED_EXCEPTION = "expiredPasswordException";
    
    public static final String LBL_PASSWORD_RANDOM_LENGTH = "password.random.length";
    
    public static final String LBL_PASSWORD_RANDOM_CONSTRAINTS = "password.random.constraints";
    
    public static final String LBL_PASSWORD_CHANGE_UNAVAILABLE = "password.change.unavailable";
    
    public static final String LBL_PASSWORD_CHANGE_PAGE_TITLE = "password.change.dialog.panel.title";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}
