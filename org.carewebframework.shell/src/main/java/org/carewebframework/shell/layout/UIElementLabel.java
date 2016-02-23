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

import org.carewebframework.theme.ThemeUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zul.Label;

/**
 * Simple button stock object.
 */
public class UIElementLabel extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementLabel.class, UIElementBase.class);
    }
    
    private final Label label = new Label();
    
    private ThemeUtil.LabelSize size = ThemeUtil.LabelSize.DEFAULT;
    
    private ThemeUtil.LabelStyle style = ThemeUtil.LabelStyle.DEFAULT;
    
    public UIElementLabel() {
        setOuterComponent(label);
        updateStyle();
    }
    
    private void updateStyle() {
        ThemeUtil.applyThemeClass(label, "label", style, size);
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
        return label.getValue();
    }
    
    /**
     * Sets the button's label text.
     * 
     * @param value The label text.
     */
    public void setLabel(String value) {
        label.setValue(value);
    }
    
    public ThemeUtil.LabelSize getSize() {
        return size;
    }
    
    public void setSize(ThemeUtil.LabelSize size) {
        this.size = size;
        updateStyle();
    }
    
    public ThemeUtil.LabelStyle getStyle() {
        return style;
    }
    
    public void setStyle(ThemeUtil.LabelStyle style) {
        this.style = style;
        updateStyle();
    }
    
    @Override
    protected void applyColor() {
        ZKUtil.updateStyle(label, "color", getColor());
    }
    
}
