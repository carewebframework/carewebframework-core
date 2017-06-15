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
package org.carewebframework.shell.elements;

import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Image;

/**
 * Simple button stock object.
 */
public class ElementImage extends ElementUI {
    
    static {
        registerAllowedParentClass(ElementImage.class, ElementUI.class);
    }
    
    private final Div root = new Div();
    
    private final Image image = new Image();
    
    private boolean stretch;
    
    public ElementImage() {
        setOuterComponent(root);
        root.addClass("cwf-plugin-container");
        fullSize(root);
        root.addChild(image);
        associateComponent(image);
    }
    
    /**
     * Sets the URL of the image to display.
     *
     * @param url Image URL.
     */
    public void setUrl(String url) {
        image.setSrc(url);
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
