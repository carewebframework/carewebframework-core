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

import org.zkoss.zul.Div;
import org.zkoss.zul.Image;

/**
 * Simple button stock object.
 */
public class UIElementImage extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementImage.class, UIElementBase.class);
    }
    
    private final Div root = new Div();
    
    private final Image image = new Image();
    
    private boolean stretch;
    
    public UIElementImage() {
        setOuterComponent(root);
        root.setZclass("cwf-plugin-container");
        fullSize(root);
        root.appendChild(image);
        associateComponent(image);
    }
    
    /**
     * Sets the URL of the image to display.
     * 
     * @param url Image URL.
     */
    public void setUrl(String url) {
        image.setSrc(url);
        image.invalidate();
    }
    
    /**
     * Returns the URL of the image.
     * 
     * @return Image URL.
     */
    public String getUrl() {
        return image.getSrc();
    }
    
    /**
     * Returns whether or not to stretch the image to fill its parent.
     * 
     * @return Stretch setting.
     */
    public boolean getStretch() {
        return stretch;
    }
    
    /**
     * Sets whether or not to stretch the image to fill its parent.
     * 
     * @param stretch Stretch setting.
     */
    public void setStretch(boolean stretch) {
        this.stretch = stretch;
        image.setWidth(stretch ? "100%" : null);
        image.setHeight(stretch ? "100%" : null);
    }
    
}
