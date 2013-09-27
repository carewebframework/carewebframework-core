/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.zkoss.zk.ui.Component;

/**
 * Static class of convenience methods for drag-and-drop support.
 */
public class DropUtil {
    
    private static final String DROP_RENDERER_ATTR = "@drop_renderer";
    
    /**
     * Searches for a drop renderer associated with the specified component or one of its ancestor
     * components.
     * 
     * @param component Component whose drop renderer is sought.
     * @return The associated drop renderer, or null if none found.
     */
    public static IDropRenderer getDropRenderer(Component component) {
        IDropRenderer dropRenderer = null;
        
        while (dropRenderer == null && component != null) {
            dropRenderer = (IDropRenderer) component.getAttribute(DROP_RENDERER_ATTR);
            component = component.getParent();
        }
        
        return dropRenderer;
    }
    
    /**
     * Associates a drop renderer with a component.
     * 
     * @param component Component to be associated.
     * @param dropRenderer Drop renderer to associate.
     */
    public static void setDropRenderer(Component component, IDropRenderer dropRenderer) {
        if (dropRenderer == null) {
            component.removeAttribute(DROP_RENDERER_ATTR);
        } else {
            component.setAttribute(DROP_RENDERER_ATTR, dropRenderer);
        }
    }
    
    /**
     * Enforces static class.
     */
    private DropUtil() {
    };
}
