/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.mock.security.impl;

import org.carewebframework.ui.zk.ZKUtil;

/**
 * Package-wide constants.
 * 
 * @author dmartin
 */
public final class Constants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
    public static final String DIALOG_ACCESS_DENIED = RESOURCE_PREFIX + "accessDenied.zul";
    
    public static final String LOGIN_PROPERTIES_PREFIX = "LOGIN.";
    
    public static final String ROLE_PREFIX = org.carewebframework.security.spring.Constants.ROLE_PREFIX;
    
    public static final String PRIV_PREFIX = org.carewebframework.security.spring.Constants.PRIV_PREFIX;
    
    public static final String DEFAULT_INSTITUTION = "defaultInstitution";
    
    public static final String DEFAULT_USERNAME = "defaultUsername";
    
    public static final String EXPIRED_PASSWORD_EXCEPTION = "expiredPasswordException";
    
    public static final String LBL_LOGIN_ERROR = "login.error";
    
    public static final String LBL_LOGIN_FORM_TIMEOUT_MESSAGE = "login.form.timeout.message";
    
    public static final String LBL_LOGIN_PROGRESS = "login.progress";
    
    public static final String LBL_LOGIN_REQUIRED_FIELDS = "login.required.fields";
    
    public static final String LBL_LOGIN_NO_VALID_INSTITUTIONS = "login.no.valid.institutions";
    
    public static final String LBL_LOGIN_ERROR_INVALID = "login.error.invalid";
    
    public static final String LBL_LOGIN_ERROR_EXPIRED_PASSWORD = "login.error.expired.password";
    
    public static final String LBL_LOGIN_ERROR_EXPIRED_USER = "login.error.expired.user";
    
    public static final String LBL_LOGIN_ERROR_UNEXPECTED = "login.error.unexpected";
    
    public static final String LBL_PASSWORD_RANDOM_CHARACTER_LENGTH = "password.random.character.length";
    
    public static final String LBL_CHANGE_PASSWORD_UNAVAILABLE = "change.password.dialog.unavailable";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
    
}
