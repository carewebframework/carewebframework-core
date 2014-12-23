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

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Span;

/**
 * A child of a UIElementTreeView, this UI element specifies the tree path where its associated tree
 * node is to reside in the parent's tree.
 */
public class UIElementTreePane extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementTreePane.class, UIElementTreeView.class);
        registerAllowedParentClass(UIElementTreePane.class, UIElementTreePane.class);
        registerAllowedChildClass(UIElementTreePane.class, UIElementBase.class);
    }
    
    private final EventListener<MouseEvent> clickListener = new EventListener<MouseEvent>() {
        
        @Override
        public void onEvent(MouseEvent event) throws Exception {
            if (canOpen && event.getX() < 20) {
                setOpen(!open);
            } else {
                treeView.setActivePane(UIElementTreePane.this);
            }
        }
        
    };
    
    private final Div pane = new Div();
    
    private final Span node;
    
    private final A anchor;
    
    private UIElementBase mainChild;
    
    private UIElementBase activeChild;
    
    private UIElementTreeView treeView;
    
    private boolean open = true;
    
    private boolean canOpen;
    
    public UIElementTreePane() {
        super();
        maxChildren = Integer.MAX_VALUE;
        fullSize(pane);
        pane.setVisible(false);
        setOuterComponent(pane);
        node = (Span) createFromTemplate();
        associateComponent(node);
        anchor = (A) node.getFirstChild();
        anchor.addEventListener(Events.ON_CLICK, clickListener);
        associateComponent(anchor);
    }
    
    private void setSelected(boolean selected) {
        ZKUtil.toggleSclass(anchor, "btn-primary", "btn-default", selected);
    }
    
    private void setOpen(boolean open) {
        this.open = open;
        
        if (!canOpen) {
            node.setSclass(null);
        } else {
            ZKUtil.toggleSclass(node, "cwf-treeview-node-exp", "cwf-treeview-node-col", open);
        }
    }
    
    private void checkChildren() {
        UIElementBase child = getFirstVisibleChild();
        
        while (child != null && !(child instanceof UIElementTreePane)) {
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
        setSelected(true);
        treeView.setActivePane(this);
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);
        setSelected(activated);
        node.setVisible(visible);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ZKUtil.updateSclass(anchor, "cwf-treeview-node-disabled", enabled);
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
    protected void afterAddChild(UIElementBase child) {
        super.afterAddChild(child);
        
        if (!(child instanceof UIElementTreePane)) {
            mainChild = child;
        }
        
        if (child instanceof UIElementTreePane) {
            checkChildren();
        }
    }
    
    @Override
    protected void afterRemoveChild(UIElementBase child) {
        super.afterRemoveChild(child);
        
        if (mainChild == child) {
            mainChild = null;
        }
        
        if (activeChild == child) {
            activeChild = null;
        }
        
        if (child instanceof UIElementTreePane) {
            checkChildren();
        }
    }
    
    @Override
    public boolean canAcceptChild(Class<? extends UIElementBase> clazz) {
        return super.canAcceptChild(clazz) && checkChildClass(clazz);
    }
    
    @Override
    public boolean canAcceptChild(UIElementBase child) {
        return super.canAcceptChild(child) && checkChildClass(child.getClass());
    }
    
    @Override
    public void bind() {
        setTreeView(getAncestor(UIElementTreeView.class));
        treeView.getInnerComponent().appendChild(pane);
        UIElementBase parent = getParent();
        Component root = parent == treeView ? treeView.getSelector() : ((UIElementTreePane) parent).node;
        root.appendChild(node);
    }
    
    /**
     * Associates the pane with the specified tree view. Recurses over immediate children to do the
     * same.
     * 
     * @param treeView Tree view.
     */
    private void setTreeView(UIElementTreeView treeView) {
        if (this.treeView != treeView) {
            this.treeView = treeView;
            
            for (UIElementBase child : getChildren()) {
                if (child instanceof UIElementTreePane) {
                    ((UIElementTreePane) child).setTreeView(treeView);
                }
            }
        }
    }
    
    /**
     * Remove the node from the tree view when this element is destroyed.
     */
    @Override
    public void unbind() {
        node.detach();
        pane.detach();
    }
    
    /**
     * The caption label is the instance name.
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }
    
    @Override
    public void afterMoveTo(int index) {
        moveChild(node, index + 1);
    }
    
    @Override
    protected void applyHint() {
        applyHint(node);
    }
    
    /*package*/void makeActivePane(boolean active) {
        if (!active) {
            activate(false);
            activeChild = null;
        } else {
            activeChild = mainChild;
            UIElementBase child = this;
            UIElementBase parent = getParent();
            
            while (parent instanceof UIElementTreePane) {
                ((UIElementTreePane) parent).activeChild = child;
                child = parent;
                parent = parent.getParent();
            }
            
            parent.activate(true);
        }
        
        setSelected(active);
    }
    
    private boolean checkChildClass(Class<? extends UIElementBase> clazz) {
        if (clazz != UIElementTreePane.class && mainChild != null) {
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
