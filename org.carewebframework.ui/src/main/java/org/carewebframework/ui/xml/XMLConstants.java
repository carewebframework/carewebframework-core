/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.xml;

import org.carewebframework.ui.zk.ZKUtil;

/**
 * Static utility class for XML viewing constants.
 */
public class XMLConstants {
    
    protected static final String RESOURCE_PATH = ZKUtil.getResourcePath(XMLConstants.class);
    
    protected static final String VIEW_DIALOG = RESOURCE_PATH + "XMLViewer.zul";
    
    protected static final String STYLE_TAG = "cwf-xml-tag";
    
    protected static final String STYLE_ATTR_NAME = "cwf-xml-attrname";
    
    protected static final String STYLE_ATTR_VALUE = "cwf-xml-attrvalue";
    
    protected static final String STYLE_CONTENT = "cwf-xml-content";
    
    public static final String[] EXCLUDED_PROPERTIES = { "stubonly=inherit", "mold=default", "renderdefer=-1",
            "draggable=false", "droppable=false", "zindex=-1", "ZIndex=-1", "widgetClass", "menu.src" };
    
    /**
     * Enforce static class.
     */
    private XMLConstants() {
    }
}
