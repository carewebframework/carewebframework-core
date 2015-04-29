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
 * Interface to be implemented by handlers of drop events.
 */
public interface IDropHandler {
    
    /**
     * Handler returns the id(s) of the drop types it can accept. Separate multiple drop ids with
     * commas.
     * 
     * @return Comma-delimited list of acceptable drop ids.
     */
    String getDropId();
    
    /**
     * Simulates a drop event. The handler will process the item as if it had been dropped by user
     * action.
     * 
     * @param droppedItem The dropped item.
     */
    void drop(Component droppedItem);
}
