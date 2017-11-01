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

import org.fujion.component.Pane;

/**
 * A child of the ElementSplitterView.
 */
public class ElementSplitterPane extends ElementUI {
    
    static {
        registerAllowedParentClass(ElementSplitterPane.class, ElementSplitterView.class);
        registerAllowedChildClass(ElementSplitterPane.class, ElementUI.class, 1);
    }
    
    private final Pane pane = new Pane();
    
    private int size = 1;
    
    private boolean relative = true;
    
    private boolean resizable = true;

    public ElementSplitterPane() {
        super();
        setResizable(resizable);
        setOuterComponent(pane);
        updateSize(true);
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
        updateSize();
    }
    
    @Override
    public String getInstanceName() {
        return "Pane #" + (getParent().indexOfChild(this) + 1);
    }
    
    public void setRelative(boolean relative) {
        this.relative = relative;
        updateSize();
    }
    
    public boolean isRelative() {
        return relative;
    }
    
    public String getCaption() {
        return pane.getTitle();
    }
    
    public void setCaption(String caption) {
        pane.setTitle(caption);
    }
    
    /*package*/ void updateSize(boolean isHorizontal) {
        pane.setFlex(relative ? Integer.toString(size) : null);
        pane.setHeight(relative || isHorizontal ? null : size + "px");
        pane.setWidth(relative || !isHorizontal ? null : size + "px");
    }
    
    private void updateSize() {
        if (getParent() != null) {
            updateSize(((ElementSplitterView) getParent()).isHorizontal());
        }
    }
    
    public boolean isResizable() {
        return resizable;
    }
    
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        pane.setSplittable(resizable || isDesignMode());
    }
    
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        setResizable(resizable);
    }
    
}
