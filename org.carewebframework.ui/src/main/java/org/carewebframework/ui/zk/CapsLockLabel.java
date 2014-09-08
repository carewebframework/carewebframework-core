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

import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

/**
 * Displays a warning message when caps lock is activated when entering data in a text box. Note
 * that JavaScript cannot directly determine the state of the caps lock key. It can only infer it by
 * the receipt of an upper case key code in the absence of shift key activation. Thus, this is not a
 * perfect solution.
 */
public class CapsLockLabel extends Label {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox textbox;
    
    @Override
    protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
        super.renderProperties(renderer);
        render(renderer, "textbox", textbox);
    }
    
    /**
     * Sets the text box to be monitored.
     * 
     * @param textbox The text box.
     */
    public void setTextbox(Textbox textbox) {
        this.textbox = textbox;
        smartUpdate("textbox", textbox == null ? null : textbox);
    }
    
    /**
     * Returns the text box being monitored.
     * 
     * @return A text box.
     */
    public Textbox getTextbox() {
        return textbox;
    }
}
