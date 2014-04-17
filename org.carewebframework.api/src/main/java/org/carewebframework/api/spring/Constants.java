/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

/**
 * Constants class
 */
public class Constants {
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context.
     */
    public static final String PROFILE_ROOT = "root";
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context, in a production setting.
     */
    public static final String PROFILE_ROOT_PROD = "root-prod";
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context, in a test setting.
     */
    public static final String PROFILE_ROOT_TEST = "root-test";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context.
     */
    public static final String PROFILE_DESKTOP = "desktop";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a production setting.
     */
    public static final String PROFILE_DESKTOP_PROD = "desktop-prod";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a test setting.
     */
    public static final String PROFILE_DESKTOP_TEST = "desktop-test";
    
    /**
     * All root profiles.
     */
    public static final String[] PROFILES_ROOT = { PROFILE_ROOT, PROFILE_ROOT_PROD, PROFILE_ROOT_TEST };
    
    /**
     * All desktop profiles.
     */
    public static final String[] PROFILES_DESKTOP = { PROFILE_DESKTOP, PROFILE_DESKTOP_PROD, PROFILE_DESKTOP_TEST };
    
    /**
     * All production profiles.
     */
    public static final String[] PROFILES_PROD = { PROFILE_ROOT, PROFILE_ROOT_PROD, PROFILE_DESKTOP, PROFILE_DESKTOP_PROD };
    
    /**
     * All test profiles.
     */
    public static final String[] PROFILES_TEST = { PROFILE_ROOT, PROFILE_ROOT_TEST, PROFILE_DESKTOP, PROFILE_DESKTOP_TEST };
    
    /**
     * All production root profiles.
     */
    public static final String[] PROFILES_ROOT_PROD = { PROFILE_ROOT, PROFILE_ROOT_PROD };
    
    /**
     * All production desktop profiles.
     */
    public static final String[] PROFILES_DESKTOP_PROD = { PROFILE_DESKTOP, PROFILE_DESKTOP_PROD };
    
    /**
     * All test root profiles.
     */
    public static final String[] PROFILES_ROOT_TEST = { PROFILE_ROOT, PROFILE_ROOT_TEST };
    
    /**
     * All test desktop profiles.
     */
    public static final String[] PROFILES_DESKTOP_TEST = { PROFILE_DESKTOP, PROFILE_DESKTOP_TEST };
    
    /**
     * Default locations to search for configurations files for the Spring application context.
     */
    public static final String[] DEFAULT_LOCATIONS = { "classpath*:/META-INF/*-spring.xml",
            "classpath*:/META-INF/spring/*.xml" };
    
    /**
     * Enforce static class.
     */
    private Constants() {
    }
    
}
