/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import org.carewebframework.shell.Constants;
import org.carewebframework.ui.zk.ZKUtil;

/**
 * Package-wide constants.
 */
public class LayoutConstants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(LayoutConstants.class);
    
    public static final String LAYOUT_ROOT = "layout";
    
    public static final String PATH_DELIMITER = "\\\\";
    
    protected static final String PROPERTY_LAYOUT_SHARED = "CAREWEB.LAYOUT.SHARED";
    
    protected static final String PROPERTY_LAYOUT_PRIVATE = "CAREWEB.LAYOUT.PRIVATE";
    
    protected static final String PROPERTY_LAYOUT_ASSOCIATION = "CAREWEB.LAYOUT.ASSOCIATION";
    
    public static final String EVENT_ELEMENT_ACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.ACTIVATE";
    
    public static final String EVENT_ELEMENT_INACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.INACTIVATE";
    
    public static final String LAYOUT_VERSION = "3.0";
    
    /**
     * Enforce static class.
     */
    private LayoutConstants() {
    };
}
