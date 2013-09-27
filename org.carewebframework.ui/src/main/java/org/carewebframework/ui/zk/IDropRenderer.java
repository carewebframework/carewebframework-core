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
 * Interface to support rendering of dropped items.
 */
public interface IDropRenderer {
    
    /**
     * The drop renderer should fully render the dropped item and return its root component. The
     * caller will attach the returned root component to an appropriate parent.
     * 
     * @param droppedItem The item that was dropped.
     * @return The root component of the rendered view. The implementation may return null,
     *         indicating an inability to render the dropped item for any reason.
     */
    Component renderDroppedItem(Component droppedItem);
    
    /**
     * The drop renderer should supply text to be displayed in association with the dropped item.
     * 
     * @param droppedItem
     * @return The display text.
     */
    String getDisplayText(Component droppedItem);
    
    /**
     * The drop renderer may return a value of false to temporarily disable its participation in
     * drop events.
     * 
     * @return Enabled status.
     */
    boolean isEnabled();
}
