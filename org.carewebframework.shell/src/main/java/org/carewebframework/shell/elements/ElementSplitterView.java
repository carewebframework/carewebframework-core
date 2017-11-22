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

import org.carewebframework.shell.designer.PropertyEditorSplitterView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.fujion.component.Paneview;
import org.fujion.component.Paneview.Orientation;

/**
 * A splitter view has either a vertical or horizontal orientation and can contain any number of
 * splitter panes which are placed side-by-side with splitter bars in between for manual sizing.
 */
public class ElementSplitterView extends ElementUI {

    static {
        registerAllowedParentClass(ElementSplitterView.class, ElementUI.class);
        registerAllowedChildClass(ElementSplitterView.class, ElementSplitterPane.class, Integer.MAX_VALUE);
        PropertyTypeRegistry.register("panes", PropertyEditorSplitterView.class);
    }

    private final Paneview root = new Paneview();

    private Orientation orientation;

    public ElementSplitterView() {
        super();
        setOuterComponent(root);
        fullSize(root);
        root.setFlex("1");
        setOrientation("horizontal");
    }

    public void setOrientation(String orientation) {
        this.orientation = Orientation.valueOf(orientation.toUpperCase());
        root.setOrientation(this.orientation);
        boolean isHorizontal = isHorizontal();

        for (ElementSplitterPane child : this.getChildren(ElementSplitterPane.class)) {
            child.updateSize(isHorizontal);
        }
    }

    @Override
    protected void afterAddChild(ElementBase child) {
        super.afterAddChild(child);
        ((ElementSplitterPane) child).updateSize(isHorizontal());
    }

    public String getOrientation() {
        return orientation.toString();
    }

    public boolean isHorizontal() {
        return orientation == Orientation.HORIZONTAL;
    }

}
