/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import org.carewebframework.ui.zk.ZKUtil;

/**
 * Constants class
 */
public class Constants {
    
    /**
     * Primarily used to locate the embedded 'web' resources.
     */
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
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
