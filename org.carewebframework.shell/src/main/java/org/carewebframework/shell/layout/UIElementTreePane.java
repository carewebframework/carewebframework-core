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

import org.carewebframework.ui.zk.TreeUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

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
    
    private final Div pane = new Div();
    
    private final Treeitem node = new Treeitem();
    
    private UIElementBase mainChild;
    
    private UIElementBase activeChild;
    
    private UIElementTreeView treeView;
    
    public UIElementTreePane() {
        super();
        maxChildren = Integer.MAX_VALUE;
        fullSize(pane);
        pane.setVisible(false);
        setOuterComponent(pane);
        node.appendChild(new Treechildren());
        associateComponent(node);
        ZKUtil.setCustomColorLogic(node, "jq(this).find('.z-treecell-content').css('color',value?value:'');");
    }
    
    @Override
    public void bringToFront() {
        super.bringToFront();
        node.setSelected(true);
        treeView.setActivePane(this);
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);
        node.setSelected(activated);
        node.setVisible(visible);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Treerow tr = node.getTreerow();
        
        if (tr == null) {
            node.appendChild(tr = new Treerow());
        }
        
        ZKUtil.updateSclass(tr, "cwf-tree-node-disabled", enabled);
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
        UIElementBase parent = getParent();
        Tree tree = treeView == null ? null : treeView.getTree();
        Treechildren tc = parent == treeView ? tree.getTreechildren() : ((UIElementTreePane) parent).node.getTreechildren();
        UIElementBase sib = getNextSibling(false);
        
        while (sib != null && !(sib instanceof UIElementTreePane)) {
            sib = sib.getNextSibling(false);
        }
        
        Component refNode = sib instanceof UIElementTreePane ? ((UIElementTreePane) sib).node : null;
        tc.insertBefore(node, refNode);
        
        if (tree != null) {
            TreeUtil.adjustVisibility(tree);
        }
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
            treeView.getInnerComponent().appendChild(pane);
            
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
        
        if (treeView != null) {
            TreeUtil.adjustVisibility(treeView.getTree());
            treeView = null;
        }
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
        super.afterMoveTo(index);
        moveChild(node, index);
    }
    
    @Override
    protected void applyColor() {
        applyColor(pane);
        applyColor(node);
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
    }
    
    private boolean checkChildClass(Class<? extends UIElementBase> clazz) {
        if (clazz != UIElementTreePane.class && mainChild != null) {
            setRejectReason("Tree pane can accept only one child of this type.");
            return false;
        }
        
        return true;
    }
    
    public String getLabel() {
        return node.getLabel();
    }
    
    public void setLabel(String label) {
        node.setLabel(label);
    }
}
