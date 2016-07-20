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
