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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.ui.action.ActionListener;

/**
 * Base class for UI elements that support an associated action.
 */
public class UIElementActionBase extends UIElementZKBase {
    
    private String action;
    
    private ActionListener listener;
    
    public void setAction(String action) {
        if (!StringUtils.equals(action, this.action)) {
            this.action = action;
            listener = ActionListener.addAction(getOuterComponent(), action);
            updateListener();
        }
    }
    
    public String getAction() {
        return action;
    }
    
    /**
     * Disable action in design mode.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        updateListener();
    }
    
    /**
     * Disables the event listener when in design mode.
     */
    protected void updateListener() {
        if (listener != null) {
            listener.setDisabled(isDesignMode());
        }
    }
}
