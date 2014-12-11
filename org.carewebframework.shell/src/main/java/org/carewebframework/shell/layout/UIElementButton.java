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

import org.carewebframework.shell.themes.ThemeUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zul.A;
import org.zkoss.zul.impl.LabelImageElement;

/**
 * Simple button stock object.
 */
public class UIElementButton extends UIElementActionBase {
    
    static {
        registerAllowedParentClass(UIElementButton.class, UIElementBase.class);
    }
    
    private final LabelImageElement component;
    
    private ThemeUtil.ButtonSize size;
    
    private ThemeUtil.ButtonStyle style;
    
    public UIElementButton() {
        this(new A(), ThemeUtil.ButtonSize.DEFAULT, ThemeUtil.ButtonStyle.DEFAULT);
    }
    
    public UIElementButton(LabelImageElement component, ThemeUtil.ButtonSize size, ThemeUtil.ButtonStyle style) {
        this.component = component;
        this.size = size;
        this.style = style;
        setOuterComponent(component);
        updateStyle();
    }
    
    private void updateStyle() {
        ThemeUtil.applyThemeClass(component, "btn", style, size);
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
        return component.getLabel();
    }
    
    /**
     * Sets the button's label text.
     * 
     * @param value The label text.
     */
    public void setLabel(String value) {
        component.setLabel(value);
    }
    
    /**
     * Sets the URL of the icon to be display on the button.
     * 
     * @param url Icon URL.
     */
    public void setIcon(String url) {
        component.setImage(url);
        component.invalidate();
    }
    
    /**
     * Returns the URL of the icon to be display on the button.
     * 
     * @return Icon URL.
     */
    public String getIcon() {
        return component.getImage();
    }
    
    public ThemeUtil.ButtonSize getSize() {
        return size;
    }
    
    public void setSize(ThemeUtil.ButtonSize size) {
        this.size = size;
        updateStyle();
    }
    
    public ThemeUtil.ButtonStyle getStyle() {
        return style;
    }
    
    public void setStyle(ThemeUtil.ButtonStyle style) {
        this.style = style;
        updateStyle();
    }
    
    @Override
    protected void applyColor() {
        ZKUtil.updateStyle(component, "color", getColor());
    }
    
}
