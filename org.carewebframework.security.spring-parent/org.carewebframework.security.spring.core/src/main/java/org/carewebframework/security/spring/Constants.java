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

public class Constants {
    
    public static final String ROLE_PREFIX = "ROLE_";
    
    public static final String PRIV_PREFIX = "PRIV_";
    
    public static final String ROLE_USER = ROLE_PREFIX + "USER";
    
    public static final String PRIV_DEBUG = PRIV_PREFIX + "DEBUG"; // Privilege that enables debug modes.
    
    public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
    
    public static final String DEFAULT_TARGET_PARAMETER = "spring-security-redirect";
    
    public static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    
    public static final String LOGOUT_TARGET = "/zkau/web/org/carewebframework/security/spring/logoutWindow.dsp";
    
    public static final String LOGOUT_URI = "/logout?" + DEFAULT_TARGET_PARAMETER + "=";
    
    public static final String LOGOUT_WARNING_ATTR = "logoutWarningMessage";
    
    public static final String LOGOUT_TARGET_ATTR = "logoutTargetURI";
    
    public static final String LBL_LOGOUT_MESSAGE_DEFAULT = "logout.message.default";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}
