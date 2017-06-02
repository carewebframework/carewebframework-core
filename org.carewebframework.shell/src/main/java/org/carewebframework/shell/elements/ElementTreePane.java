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

import org.carewebframework.theme.ThemeUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Hyperlink;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Span;
import org.carewebframework.web.event.IEventListener;

/**
 * A child of a ElementTreeView, this UI element specifies the tree path where its associated tree
 * node is to reside in the parent's tree.
 */
public class ElementTreePane extends ElementBase {

    static {
        registerAllowedParentClass(ElementTreePane.class, ElementTreeView.class);
        registerAllowedParentClass(ElementTreePane.class, ElementTreePane.class);
        registerAllowedChildClass(ElementTreePane.class, ElementBase.class);
    }

    private final Div pane = new Div();

    private final Span node;

    private final Hyperlink anchor;

    private ElementBase mainChild;

    private ElementBase activeChild;

    private ElementTreeView treeView;

    private boolean selected;

    private boolean open = true;

    private boolean canOpen;

    /**
     * Handler for node click events. Click will select the node and associated pane.
     */
    private final IEventListener clickListener = (event) -> {
        treeView.setActivePane(ElementTreePane.this);
    };

    /**
     * Handler for node double click events. Double click will toggle the node's drop down state
     */
    private final IEventListener dblclickListener = (event) -> {
        if (canOpen) {
            setOpen(!open);
        }
    };

    public ElementTreePane() {
        super();
        maxChildren = Integer.MAX_VALUE;
        //fullSize(pane);
        pane.setFlex("1");
        pane.setVisible(false);
        setOuterComponent(pane);
        node = (Span) createFromTemplate();
        associateComponent(node);
        anchor = (Hyperlink) node.getFirstChild();
        anchor.addEventListener("click", clickListener);
        anchor.addEventListener("dblclick", dblclickListener);
        associateComponent(anchor);
    }

    /**
     * Sets the selection state of the node.
     *
     * @param selected The selection state.
     */
    private void setSelected(boolean selected) {
        this.selected = selected;
        anchor.toggleClass(treeView.getSelectionStyle().getThemeClass(), "btn-default", selected);
    }

    /**
     * Called by tree view when the selection style is changed.
     *
     * @param oldStyle The old selection style.
     * @param newStyle The new selection style.
     */
    /* package */void updateSelectionStyle(ThemeUtil.ButtonStyle oldStyle, ThemeUtil.ButtonStyle newStyle) {
        if (selected) {
            anchor.toggleClass(newStyle.getThemeClass(), oldStyle.getThemeClass(), true);
        }
    }

    /**
     * Sets the drop down state of the node.
     *
     * @param open If true, show child nodes. If false, hide child nodes.
     */
    private void setOpen(boolean open) {
        this.open = open;

        if (!canOpen) {
            node.setClasses(null);
        } else {
            node.toggleClass("cwf-treeview-node-exp", "cwf-treeview-node-col", open);
        }
    }

    /**
     * Determines canOpen state based on whether any visible child nodes are present.
     */
    private void checkChildren() {
        ElementBase child = getFirstVisibleChild();

        while (child != null && !(child instanceof ElementTreePane)) {
            child = child.getNextSibling(true);
        }

        boolean oldOpen = canOpen;
        canOpen = child != null;

        if (oldOpen != canOpen) {
            setOpen(open);
        }
    }

    @Override
    public void bringToFront() {
        super.bringToFront();
        treeView.setActivePane(this);
    }

    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);
        setSelected(activated);
        node.setVisible(visible);
    }

    /**
     * Sets the enabled state of the node.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        anchor.toggleClass(null, "cwf-treeview-node-disabled", enabled);
    }

    /**
     * Forward only to active child.
     */
    @Override
    public void activateChildren(boolean activate) {
        if (activeChild != null) {
            activeChild.activate(activate);
        }
    }

    /**
     * Apply/remove the design context menu to/from both the pane and its associated node.
     *
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    @Override
    protected void setDesignContextMenu(Menupopup contextMenu) {
        setDesignContextMenu(node, contextMenu);
        setDesignContextMenu(pane, contextMenu);
    }

    /**
     * Initializes the child after it is added.
     */
    @Override
    protected void afterAddChild(ElementBase child) {
        super.afterAddChild(child);

        if (child instanceof ElementTreePane) {
            checkChildren();
        } else {
            mainChild = child;

            if (isActivated()) {
                activeChild = mainChild;
            }
        }
    }

    @Override
    protected void afterRemoveChild(ElementBase child) {
        super.afterRemoveChild(child);

        if (mainChild == child) {
            mainChild = null;
        }

        if (activeChild == child) {
            activeChild = null;
        }

        if (child instanceof ElementTreePane) {
            checkChildren();
        }
    }

    @Override
    public boolean canAcceptChild(Class<? extends ElementBase> clazz) {
        return super.canAcceptChild(clazz) && checkChildClass(clazz);
    }

    @Override
    public boolean canAcceptChild(ElementBase child) {
        return super.canAcceptChild(child) && checkChildClass(child.getClass());
    }

    @Override
    public void bind() {
        setTreeView(getAncestor(ElementTreeView.class));
        treeView.getInnerComponent().addChild(pane);
        getNodeParent().addChild(node);
    }

    /**
     * Returns the parent component for this node.
     *
     * @return The parent for this node.
     */
    private BaseComponent getNodeParent() {
        ElementBase parent = getParent();
        return parent == treeView ? treeView.getSelector() : ((ElementTreePane) parent).node;
    }

    /**
     * Associates the pane with the specified tree view. Recurses over immediate children to do the
     * same.
     *
     * @param treeView Tree view.
     */
    private void setTreeView(ElementTreeView treeView) {
        if (this.treeView != treeView) {
            this.treeView = treeView;

            for (ElementBase child : getChildren()) {
                if (child instanceof ElementTreePane) {
                    ((ElementTreePane) child).setTreeView(treeView);
                }
            }

            if (selected && treeView != null) {
                setSelected(selected);
            }
        }
    }

    /**
     * Remove the node from the tree view when this element is destroyed.
     */
    @Override
    public void unbind() {
        node.destroy();
        pane.destroy();
    }

    /**
     * The caption label is the instance name.
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }

    /**
     * Only the node needs to be resequenced, since pane sequencing is arbitrary.
     */
    @Override
    protected void afterMoveChild(ElementBase child, ElementBase before) {
        ElementTreePane childpane = (ElementTreePane) child;
        ElementTreePane beforepane = (ElementTreePane) before;
        moveChild(childpane.node, beforepane.node);
    }

    /*package*/ Span getNode() {
        return node;
    }

    /**
     * Apply the hint to the selector node.
     */
    @Override
    protected void applyHint() {
        applyHint(node);
    }

    /**
     * Called by tree view to activate / inactivate this node.
     *
     * @param active Desired activation state.
     */
    /*package*/void makeActivePane(boolean active) {
        if (!active) {
            activate(false);
            activeChild = null;
        } else {
            activeChild = mainChild;
            ElementBase child = this;
            ElementBase parent = getParent();

            while (parent instanceof ElementTreePane) {
                ((ElementTreePane) parent).activeChild = child;
                child = parent;
                parent = parent.getParent();
            }

            parent.activate(true);
        }

        setSelected(active);
    }

    /**
     * Check to be certain that child class is legit.
     *
     * @param clazz Class of child to be considered.
     * @return True if child may be added.
     */
    private boolean checkChildClass(Class<? extends ElementBase> clazz) {
        if (clazz != ElementTreePane.class && mainChild != null) {
            setRejectReason("Tree pane can accept only one child of this type.");
            return false;
        }

        return true;
    }

    public String getLabel() {
        return anchor.getLabel();
    }

    public void setLabel(String label) {
        anchor.setLabel(label);
    }

}
