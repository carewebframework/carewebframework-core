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

import org.zkoss.zul.Button;

/**
 * Simple button stock object.
 */
public class UIElementButton extends UIElementActionBase {
    
    static {
        registerAllowedParentClass(UIElementButton.class, UIElementBase.class);
    }
    
    private final Button button = new Button();
    
    public UIElementButton() {
        super();
        setOuterComponent(button);
    }
    
    @Override
    public String getInstanceName() {
        return getDisplayName() + " (" + getLabel() + ")";
    }
    
    /**
     * Returns the button's label text.
     * 
     * @return The label text.
     */
    public String getLabel() {
        return button.getLabel();
    }
    
    /**
     * Sets the button's label text.
     * 
     * @param value The label text.
     */
    public void setLabel(String value) {
        button.setLabel(value);
    }
    
    /**
     * Sets the URL of the icon to be display on the button.
     * 
     * @param url Icon URL.
     */
    public void setIcon(String url) {
        button.setImage(url);
        button.invalidate();
    }
    
    /**
     * Returns the URL of the icon to be display on the button.
     * 
     * @return Icon URL.
     */
    public String getIcon() {
        return button.getImage();
    }
    
}
