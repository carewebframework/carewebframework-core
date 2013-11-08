/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.popupsupport;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a single popup data item.
 */
public class PopupData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String title;
    
    private String message;
    
    public PopupData() {
        super();
    }
    
    /**
     * Supports the old method of serializing a popup item.
     * 
     * @param value
     */
    @Deprecated
    public PopupData(String value) {
        String pcs[] = value.toString().split("\\^", 2);
        
        if (pcs.length == 0) {
            return;
        }
        
        if (pcs.length == 1) {
            title = "";
            message = pcs[0];
        } else {
            title = pcs[0];
            message = pcs[1];
        }
    }
    
    public String getTitle() {
        return StringUtils.isEmpty(title) ? "Popup Message" : title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isEmpty() {
        return StringUtils.isEmpty(title) && StringUtils.isEmpty(message);
    }
}
