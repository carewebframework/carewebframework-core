/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.shell.layout;

import org.carewebframework.theme.ThemeUtil;
import org.carewebframework.web.component.Hyperlink;

/**
 * Simple button stock object.
 */
public class UIElementButton extends UIElementActionBase {
    
    static {
        registerAllowedParentClass(UIElementButton.class, UIElementBase.class);
    }
    
    private final Hyperlink component;
    
    private ThemeUtil.ButtonSize size;
    
    private ThemeUtil.ButtonStyle style;
    
    public UIElementButton() {
        this(new Hyperlink(), ThemeUtil.ButtonSize.SMALL, ThemeUtil.ButtonStyle.DEFAULT);
    }
    
    public UIElementButton(Hyperlink component, ThemeUtil.ButtonSize size, ThemeUtil.ButtonStyle style) {
        this.component = component;
        this.size = size;
        this.style = style;
        setOuterComponent(component);
        updateStyle();
    }
    
    private void updateStyle() {
        ThemeUtil.applyThemeClass(component, style, size);
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
     * Sets the URL of the icon to display on the button.
     * 
     * @param url Icon URL.
     */
    public void setIcon(String url) {
        component.setImage(url);
    }
    
    /**
     * Returns the URL of the icon to display on the button.
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
        component.addStyle("color", getColor());
    }
    
}
