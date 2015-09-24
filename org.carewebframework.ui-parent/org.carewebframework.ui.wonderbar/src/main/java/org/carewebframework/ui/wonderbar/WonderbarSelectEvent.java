/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.wonderbar;

import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;

/**
 * Fired when an item is selected from the wonder bar.
 */
public class WonderbarSelectEvent extends Event {
    
    public static final String ON_WONDERBAR_SELECT = "onWonderbarSelect";
    
    private static final long serialVersionUID = 1L;
    
    private final WonderbarItem selectedItem;
    
    private final int keys;
    
    /**
     * Extracts a select event from the au request.
     * 
     * @param request The AU request.
     * @return The select event.
     */
    public static final WonderbarSelectEvent getSelectEvent(AuRequest request) {
        Map<String, Object> data = request.getData();
        Desktop desktop = request.getDesktop();
        return new WonderbarSelectEvent(request.getComponent(),
                (WonderbarItem) desktop.getComponentByUuidIfAny((String) data.get("reference")), null,
                AuRequests.parseKeys(data));
    }
    
    /**
     * Creates the select event.
     * 
     * @param target Component to receive the event.
     * @param selectedItem The item that triggered the selection event.
     * @param data Arbitrary data to associate with event.
     * @param keys Keypress states at the time of selection.
     */
    public WonderbarSelectEvent(Component target, WonderbarItem selectedItem, Object data, int keys) {
        super(ON_WONDERBAR_SELECT, target, data);
        this.selectedItem = selectedItem;
        this.keys = keys;
    }
    
    /**
     * Returns the selected item.
     * 
     * @return The selected item.
     */
    public WonderbarItem getSelectedItem() {
        return selectedItem;
    }
    
    /**
     * Returns the keypress states.
     * 
     * @return The keypress states.
     */
    public int getKeys() {
        return keys;
    }
    
}
