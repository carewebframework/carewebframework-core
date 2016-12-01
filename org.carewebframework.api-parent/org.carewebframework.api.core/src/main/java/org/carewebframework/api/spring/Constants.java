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
     * Constant for beans profile which specifies the default profile for a root Spring application
     * context if none is specified.
     */
    public static final String PROFILE_ROOT_DEFAULT = PROFILE_ROOT;
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context.
     */
    public static final String PROFILE_PAGE = "page";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a production setting.
     */
    public static final String PROFILE_PAGE_PROD = "page-prod";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a test setting.
     */
    public static final String PROFILE_PAGE_TEST = "page-test";
    
    /**
     * Constant for beans profile which specifies the default profile for a child Spring application
     * context if none is specified. This is set to a non-existent profile to suppress processing a
     * bean in an unspecified profile in a child context (it should only be processed in the root
     * context).
     */
    public static final String PROFILE_PAGE_DEFAULT = "dummy";
    
    /**
     * All root profiles.
     */
    public static final String[] PROFILES_ROOT = { PROFILE_ROOT, PROFILE_ROOT_PROD, PROFILE_ROOT_TEST };
    
    /**
     * All page profiles.
     */
    public static final String[] PROFILES_PAGE = { PROFILE_PAGE, PROFILE_PAGE_PROD, PROFILE_PAGE_TEST };
    
    /**
     * All production profiles.
     */
    public static final String[] PROFILES_PROD = { PROFILE_ROOT, PROFILE_ROOT_PROD, PROFILE_PAGE, PROFILE_PAGE_PROD };
    
    /**
     * All test profiles.
     */
    public static final String[] PROFILES_TEST = { PROFILE_ROOT, PROFILE_ROOT_TEST, PROFILE_PAGE, PROFILE_PAGE_TEST };
    
    /**
     * All production root profiles.
     */
    public static final String[] PROFILES_ROOT_PROD = { PROFILE_ROOT, PROFILE_ROOT_PROD };
    
    /**
     * All production page profiles.
     */
    public static final String[] PROFILES_PAGE_PROD = { PROFILE_PAGE, PROFILE_PAGE_PROD };
    
    /**
     * All test root profiles.
     */
    public static final String[] PROFILES_ROOT_TEST = { PROFILE_ROOT, PROFILE_ROOT_TEST };
    
    /**
     * All test page profiles.
     */
    public static final String[] PROFILES_PAGE_TEST = { PROFILE_PAGE, PROFILE_PAGE_TEST };
    
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
