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

import org.carewebframework.shell.designer.PropertyEditorTreeView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.theme.ThemeUtil;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Div;
import org.fujion.component.Pane;

/**
 * A tree view consists of a selector pane on the left containing a treeview component and a view
 * area on the right where tree panes are positioned. Clicking on a node in the tree activates the
 * associated tree pane.
 */
public class ElementTreeView extends ElementUI {
    
    static {
        registerAllowedParentClass(ElementTreeView.class, ElementUI.class);
        registerAllowedChildClass(ElementTreeView.class, ElementTreePane.class, Integer.MAX_VALUE);
        PropertyTypeRegistry.register("nodes", PropertyEditorTreeView.class);
    }
    
    @WiredComponent
    private Div innerPane;
    
    @WiredComponent
    private Div selector;
    
    @WiredComponent
    private Pane selectorPane;
    
    private ElementTreePane activePane;
    
    private ThemeUtil.ButtonStyle selectionStyle = ThemeUtil.ButtonStyle.PRIMARY;
    
    public ElementTreeView() {
        super();
        setOuterComponent(createFromTemplate());
        setInnerComponent(innerPane);
    }
    
    /**
     * Only the node needs to be resequenced, since pane sequencing is arbitrary.
     */
    @Override
    protected void afterMoveChild(ElementBase child, ElementBase before) {
        ElementTreePane childpane = (ElementTreePane) child;
        ElementTreePane beforepane = (ElementTreePane) before;
        moveChild(childpane.getNode(), beforepane.getNode());
    }
    
    /**
     * Sets the caption of the selector pane.
     *
     * @param value Selector pane caption.
     */
    public void setCaption(String value) {
        selectorPane.setTitle(value);
    }
    
    /**
     * Returns the caption of the selector pane.
     *
     * @return Selector pane caption.
     */
    public String getCaption() {
        return selectorPane.getTitle();
    }
    
    /**
     * Opens or closes the selector pane.
     *
     * @param value True to open the selector pane; false to close it.
     */
    public void setOpen(boolean value) {
        //TODO: selectorPane.setOpen(value);
    }
    
    /**
     * Returns true if the selector pane is open.
     *
     * @return True if selector pane is open.
     */
    public boolean isOpen() {
        return true; //TODO: selectorPane.isOpen();
    }
    
    /**
     * Returns the button style to use for selected nodes.
     *
     * @return Button style for selected nodes.
     */
    public ThemeUtil.ButtonStyle getSelectionStyle() {
        return selectionStyle;
    }
    
    /**
     * Sets the button style to use for selected nodes.
     *
     * @param selectionStyle Button style for selected nodes.
     */
    public void setSelectionStyle(ThemeUtil.ButtonStyle selectionStyle) {
        if (activePane != null) {
            activePane.updateSelectionStyle(this.selectionStyle, selectionStyle);
        }
        
        this.selectionStyle = selectionStyle;
    }
    
    /**
     * Remove the associated tree node when a tree pane is removed.
     */
    @Override
    protected void afterRemoveChild(ElementBase child) {
        if (child == activePane) {
            setActivePane((ElementTreePane) getFirstChild());
        }
        super.afterRemoveChild(child);
    }
    
    /**
     * Returns the selector pane's container.
     *
     * @return The selector pane container.
     */
    /*package*/Div getSelector() {
        return selector;
    }
    
    /**
     * Only the active pane should receive the activation request.
     */
    @Override
    public void activateChildren(boolean activate) {
        if (activePane == null || !activePane.isVisible()) {
            ElementBase active = getFirstVisibleChild();
            setActivePane((ElementTreePane) active);
        }
        
        if (activePane != null) {
            activePane.activate(activate);
        }
    }
    
    /**
     * Activates the specified pane. Any previously active pane will be deactivated.
     *
     * @param pane Pane to make active.
     */
    protected void setActivePane(ElementTreePane pane) {
        if (pane == activePane) {
            return;
        }
        
        if (activePane != null) {
            activePane.makeActivePane(false);
        }
        
        activePane = pane;
        
        if (activePane != null) {
            activePane.makeActivePane(true);
        }
    }
}
