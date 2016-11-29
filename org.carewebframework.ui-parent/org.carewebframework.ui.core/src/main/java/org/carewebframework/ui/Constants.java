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
package org.carewebframework.ui;

import org.carewebframework.ui.core.CWFUtil;

/**
 * Constants class
 */
public class Constants {
    
    /**
     * Primarily used to locate the embedded 'web' resources.
     */
    public static final String RESOURCE_PREFIX = CWFUtil.getResourcePath(Constants.class);
    
    /**
     * Namespace/prefix for plugins (i.e. java package)
     */
    public static final String PLUGIN_NAMESPACE = "org/carewebframework/ui/component/";
    
    /**
     * Name of logging appender that captures exceptions
     */
    public static final String EXCEPTION_LOG = "EXCEPTION_LOG";
    
    /**
     * Attribute name under which a component's associated composer is stored.
     */
    public static final String ATTR_COMPOSER = RESOURCE_PREFIX + ".composer";
    
    /**
     * Events that control the desktop state.
     */
    public static final String DESKTOP_EVENT = "DESKTOP";
    
    public static final String SHUTDOWN_EVENT = DESKTOP_EVENT + ".SHUTDOWN";
    
    public static final String SHUTDOWN_ABORT_EVENT = SHUTDOWN_EVENT + ".ABORT";
    
    public static final String SHUTDOWN_START_EVENT = SHUTDOWN_EVENT + ".START";
    
    public static final String LOCK_EVENT = DESKTOP_EVENT + ".LOCK";
    
    /**
     * Event for requesting a view update.
     */
    public static final String REFRESH_EVENT = "VIEW.REFRESH";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    }
    
}
