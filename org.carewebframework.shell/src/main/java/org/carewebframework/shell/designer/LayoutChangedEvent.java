/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import org.carewebframework.shell.layout.UIElementBase;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Event signaling that a change to the layout has occurred.
 */
public class LayoutChangedEvent extends Event {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ON_LAYOUT_CHANGED = "onLayoutChanged";
    
    public LayoutChangedEvent(Component target, UIElementBase element) {
        super(ON_LAYOUT_CHANGED, target, element);
    }
    
    /**
     * Returns the UI element that changed. If null, indicates multiple elements may have changed.
     * 
     * @return The UI element that changed.
     */
    public UIElementBase getUIElement() {
        return (UIElementBase) getData();
    }
}
