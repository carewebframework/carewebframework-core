/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.infopanel;

/**
 * Simple test model object associated with dropped item. Has two properties, the itemName and the
 * itemDetail.
 */
public class DroppedItem {
    
    private final String itemName;
    
    private final String itemDetail;
    
    public DroppedItem(String itemName, String itemDetail) {
        this.itemName = itemName;
        this.itemDetail = itemDetail;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public String getItemDetail() {
        return itemDetail;
    }
    
}
