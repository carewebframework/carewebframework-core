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

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zul.A;

/**
 * Simple hyperlink stock object.
 */
public class UIElementLink extends UIElementActionBase {
    
    static {
        registerAllowedParentClass(UIElementLink.class, UIElementBase.class);
    }
    
    private final A link = new A();
    
    public UIElementLink() {
        super();
        link.setZclass("cwf-anchor");
        setOuterComponent(link);
    }
    
    @Override
    public String getInstanceName() {
        return getDisplayName() + " (" + getLabel() + ")";
    }
    
    public String getLabel() {
        return link.getLabel();
    }
    
    public void setLabel(String value) {
        link.setLabel(value);
    }
    
    @Override
    protected void applyColor() {
        ZKUtil.updateStyle(link, "color", getColor());
    }
    
    public void setIcon(String url) {
        link.setImage(url);
        link.invalidate();
    }
    
    public String getIcon() {
        return link.getImage();
    }
    
}
