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

import org.carewebframework.shell.designer.PropertyEditorTabView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.fujion.component.Tabview;
import org.fujion.component.Tabview.TabPosition;
import org.fujion.event.ChangeEvent;

/**
 * Wraps the Tabview component. This UI element can only accept ElementTabPane elements as children
 * and only one of those can be active at a time.
 */
public class ElementTabView extends ElementUI {

    static {
        registerAllowedParentClass(ElementTabView.class, ElementUI.class);
        registerAllowedChildClass(ElementTabView.class, ElementTabPane.class, Integer.MAX_VALUE);
        PropertyTypeRegistry.register("tabs", PropertyEditorTabView.class);
    }

    private final Tabview tabview = new Tabview();

    private ElementTabPane activePane;

    public ElementTabView() {
        super();
        fullSize(tabview);
        setOuterComponent(tabview);
        tabview.addClass("cwf-tabview");
        tabview.addEventListener(ChangeEvent.TYPE, (event) -> {
            setActivePane((ElementTabPane) getAssociatedElement(tabview.getSelectedTab()));
        });
    }

    /**
     * Sets the orientation which can be horizontal or vertical.
     *
     * @param orientation Orientation setting.
     */
    public void setOrientation(String orientation) {
        tabview.setTabPosition(TabPosition.valueOf(orientation.toUpperCase()));
    }

    /**
     * Returns the orientation (horizontal, vertical, top, or bottom).
     *
     * @return Orientation setting.
     */
    public String getOrientation() {
        return tabview.getTabPosition().name().toLowerCase();
    }

    /**
     * Need to detach both the tab and the tab panel of the child component.
     */
    @Override
    protected void beforeRemoveChild(ElementBase child) {
        if (child == activePane) {
            setActivePane(null);
        }
    }

    /**
     * Sets the active (visible) pane.
     *
     * @param pane The pane to become active.
     */
    protected void setActivePane(ElementTabPane pane) {
        if (pane == activePane) {
            return;
        }

        if (activePane != null) {
            activePane.activate(false);
        }

        activePane = pane;

        if (activePane != null) {
            activePane.activate(true);
        }
    }

    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);

        if (activated && visible && activePane == null && getChildCount() > 0) {
            setActivePane((ElementTabPane) getChild(0));
        }
    }
    
    /**
     * Overrides activateChildren to ensure that only the active pane is affected.
     */
    @Override
    public void activateChildren(boolean activate) {
        if (activePane == null) {
            activePane = (ElementTabPane) getAssociatedElement(tabview.getSelectedTab());
        }

        if (activePane == null || !activePane.isVisible()) {
            activePane = (ElementTabPane) getFirstVisibleChild();
        }

        if (activePane != null) {
            activePane.activate(activate);
        }
    }

}
