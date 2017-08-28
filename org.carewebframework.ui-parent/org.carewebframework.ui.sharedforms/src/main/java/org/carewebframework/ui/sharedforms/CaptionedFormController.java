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
package org.carewebframework.ui.sharedforms;

import java.util.Arrays;

import org.fujion.common.StrUtil;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Window;

/**
 * Controller for captioned form.
 */
public class CaptionedFormController extends PluginController {
    
    public enum CaptionStyle {
        HIDDEN, TITLE, FRAME, LEFT, RIGHT, CENTER;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    private CaptionStyle captionStyle = CaptionStyle.HIDDEN;
    
    @WiredComponent("^")
    private Window panel;
    
    private String color1;
    
    private String color2;
    
    @Override
    public void onLoad(ElementPlugin plugin) {
        //panel = (Window) root;
        super.onLoad(plugin);
        plugin.registerProperties(this, "caption", "captionStyle", "icon", "color1", "color2");
        updateStyle();
    }
    
    public CaptionStyle getCaptionStyle() {
        return captionStyle;
    }
    
    public void setCaptionStyle(CaptionStyle captionStyle) {
        this.captionStyle = captionStyle;
        updateStyle();
    }
    
    /**
     * Update styles by current caption style setting.
     */
    private void updateStyle() {
        CaptionStyle cs = captionStyle == null ? CaptionStyle.HIDDEN : captionStyle;
        String background = null;
        
        switch (cs) {
            case FRAME:
                break;
            
            case TITLE:
                break;
            
            case LEFT:
                background = getGradValue(color1, color2);
                break;
            
            case RIGHT:
                background = getGradValue(color2, color1);
                break;
            
            case CENTER:
                background = getGradValue(color1, color2, color1);
                break;
            
        }
        
        panel.addClass("sharedForms-captioned sharedForms-captioned-caption-" + cs.name().toLowerCase());
        String css = "##{id}-titlebar ";
        
        if (cs == CaptionStyle.HIDDEN) {
            css += "{display:none}";
        } else {
            css += "{background: " + background + "}";
        }
        
        panel.setCss(css);
    }
    
    /**
     * Returns css gradient specifier.
     *
     * @param colors List of colors for gradient.
     * @return The gradient specifier.
     */
    private String getGradValue(String... colors) {
        return "linear-gradient(to right," + StrUtil.fromList(Arrays.asList(colors), ", ", "none") + ")";
    }
    
    /**
     * Get first color in gradient range.
     *
     * @return First color in gradient range.
     */
    public String getColor1() {
        return color1;
    }
    
    /**
     * Set first color in gradient range.
     *
     * @param color1 First color in gradient range.
     */
    public void setColor1(String color1) {
        this.color1 = color1;
        updateStyle();
    }
    
    /**
     * Get second color in gradient range.
     *
     * @return Second color in gradient range.
     */
    public String getColor2() {
        return color2;
    }
    
    /**
     * Set second color in gradient range.
     *
     * @param color2 Second color in gradient range.
     */
    public void setColor2(String color2) {
        this.color2 = color2;
        updateStyle();
    }
    
    /**
     * Return the current caption.
     *
     * @return Current caption.
     */
    public String getCaption() {
        return panel.getTitle();
    }
    
    /**
     * Set the current caption.
     *
     * @param caption Current caption.
     */
    public void setCaption(String caption) {
        panel.setTitle(caption);
    }
    
    /**
     * Return the caption icon URL.
     *
     * @return Caption icon URL.
     */
    public String getIcon() {
        return panel.getImage();
    }
    
    /**
     * Set the caption icon.
     *
     * @param image URL of icon.
     */
    public void setIcon(String image) {
        panel.setImage(image);
    }
    
}
