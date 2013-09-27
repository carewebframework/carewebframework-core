/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell;

import org.carewebframework.ui.zk.ZKUtil;

/**
 * Package-wide constants.
 */
public class Constants {
    
    public static final String EVENT_PREFIX = "CAREWEB";
    
    public static final String EVENT_RESOURCE_PREFIX = EVENT_PREFIX + ".RESOURCE";
    
    public static final String EVENT_RESOURCE_PROPGROUP_PREFIX = EVENT_RESOURCE_PREFIX + ".PROPGROUP";
    
    public static final String EVENT_RESOURCE_PROPGROUP_ADD = EVENT_RESOURCE_PROPGROUP_PREFIX + ".ADD";
    
    public static final String EVENT_RESOURCE_PROPGROUP_REMOVE = EVENT_RESOURCE_PROPGROUP_PREFIX + ".REMOVE";
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(Constants.class);
    
    public static final String IMAGE_PATH = RESOURCE_PREFIX + "images/";
    
    public static final String SHELL_INSTANCE = "CAREWEB.SHELL";
    
    public static final String ATTR_CONTAINER = "CAREWEB.CONTAINER";
    
    public static final String ATTR_VISIBLE = "CAREWEB.VISIBLE";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    }
}
