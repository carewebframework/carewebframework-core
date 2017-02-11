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
package org.carewebframework.shell.designer;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.shell.elements.UIElementBase;
import org.carewebframework.shell.elements.UIElementProxy;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.util.TreeUtil;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Popup;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

/**
 * Abstract class for implementing a custom property editor based on a tree view display of child
 * elements that can be manipulated and whose properties may be edited.
 * 
 * @param <T> Type of child element.
 */
public class PropertyEditorCustomTree<T extends UIElementBase> extends PropertyEditorCustom {
    
    /**
     * Subclass UIElementProxy to allow us to synchronize changes to label property with the
     * corresponding tree node.
     */
    private class Proxy extends UIElementProxy {
        
        private Treenode node;
        
        public Proxy(T child) {
            super(child);
        }
        
        public Proxy(PluginDefinition def) {
            super(def);
        }
        
        public UIElementBase realize() throws Exception {
            Treenode parentItem = node.getParent() instanceof Treenode ? (Treenode) node.getParent() : null;
            UIElementBase parentElement = parentItem == null ? getTarget() : getProxy(parentItem).realize();
            realize(parentElement);
            return getTarget();
        }
        
        public void setItem(Treenode node) {
            this.node = node;
            node.setData(this);
            node.setLabel(getLabel());
        }
        
        /**
         * Returns the label property value.
         * 
         * @return The label text, or null if none.
         */
        public String getLabel() {
            String label = getProperty(labelProperty);
            label = label == null ? node.getLabel() : label;
            
            if (label == null) {
                label = getDefaultInstanceName();
                setProperty(labelProperty, label);
            }
            
            return label;
        }
        
        public void setLabel(String label) {
            setProperty(labelProperty, label);
            node.setLabel(label);
        }
        
        /**
         * Returns a property value.
         * 
         * @param propertyName The property name.
         * @return The property value, or null if none.
         */
        private String getProperty(String propertyName) {
            return propertyName == null ? null : (String) getPropertyValue(propertyName);
        }
        
        private String setProperty(String propertyName, String value) {
            return propertyName == null ? null : (String) setPropertyValue(propertyName, value);
        }
        
        /**
         * Return the name to be assigned to a new instance of the child component.
         * <p>
         * Subclasses may override to provide an alternate instance name to be given to newly
         * created elements.
         * 
         * @return The default instance name.
         */
        private String getDefaultInstanceName() {
            String name = getDefinition().getName() + " #";
            int i = 0;
            Treeview tree = node.getTreeview();
            
            while (TreeUtil.findNodeByLabel(tree, name + ++i, false) != null) {}
            return name + i;
        }
        
    }
    
    private static final Log log = LogFactory.getLog(PropertyEditorCustomTree.class);
    
    private static final String ITEM_ATTR = "EDITED_ITEM";
    
    private static final String LABEL_ATTR = "ITEM_LABEL";
    
    @WiredComponent
    private Button btnUp;
    
    @WiredComponent
    private Button btnDown;
    
    @WiredComponent
    private Button btnRight;
    
    @WiredComponent
    private Button btnLeft;
    
    @WiredComponent
    private Button btnDelete;
    
    @WiredComponent
    private Treeview tree;
    
    @WiredComponent
    private BaseComponent gridParent;
    
    @WiredComponent
    private Popup popLabel;
    
    @WiredComponent("popLabel.txtLabel")
    private Textbox txtLabel;
    
    private PropertyGrid propertyGrid;
    
    private Treenode currentItem;
    
    private final boolean hierarchical;
    
    private final String labelProperty;
    
    private boolean hasChanged;
    
    private boolean selectionChanging;
    
    private PropertyEditorBase<?> labelEditor;
    
    private final List<Proxy> proxies = new ArrayList<>();
    
    private final Class<T> childClass;
    
    /**
     * Create the editor instance.
     * 
     * @param childClass This is the class of child elements managed by this editor. It is used to
     *            create new instances of the UI element from within the editor and to restrict the
     *            selection of child elements to that type only.
     *            <p>
     *            The child class may be null. In this case, there is no restriction on the child
     *            type. The user is prompted for an element type when creating a new instance.
     * @param labelProperty This is the name of the property on the child UI element that will be
     *            synchronized with the label of the corresponding tree node. It may be null.
     * @param hierarchical If true, the editor assumes that child elements may contain other child
     *            elements.
     */
    public PropertyEditorCustomTree(Class<T> childClass, String labelProperty, boolean hierarchical) {
        super(DesignConstants.RESOURCE_PREFIX + "propertyEditorCustomTree.cwf");
        this.childClass = childClass;
        this.labelProperty = labelProperty;
        this.hierarchical = hierarchical;
    }
    
    /**
     * Subclasses may override to provide any special proxy initialization.
     * 
     * @param proxy Proxy to be initialized.
     */
    protected void initProxy(Proxy proxy) {
    }
    
    /**
     * Initializes the property editor by constructing the tree view based on the child elements of
     * the target element.
     * 
     * @param target Target element whose children will be represented in the tree view.
     * @param propInfo The property information associated with the child elements.
     * @param propGrid The parent property grid.
     */
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        btnRight.setVisible(hierarchical);
        btnLeft.setVisible(hierarchical);
        propertyGrid = PropertyGrid.create(null, gridParent);
        propertyGrid.getWindow().setClosable(false);
        propertyGrid.getWindow().addEventListener(ChangeEvent.TYPE, (event) -> {
            propGrid.propertyChanged();
            hasChanged = true;
        });
        //TODO: not sure if needed: propertyGrid.registerEventListener(eventType, this);
        txtLabel.setWidth("95%");
        //TODO: Needs non blank label constraint: txtLabel.setConstraint(new LabelConstraint());
        IEventListener labelEditorListener = new IEventListener() {
            
            @Override
            public void onEvent(Event event) {
                event.stopPropagation();
                editNodeStop();
            }
        };
        
        txtLabel.addEventListener(ChangeEvent.TYPE, labelEditorListener);
        txtLabel.addEventListener("blur", labelEditorListener);
        txtLabel.addEventListener("enter", labelEditorListener);
        resetTree();
    }
    
    @Override
    protected void wireController() {
        popup.wireController(this);
    }
    
    /**
     * Commits changes to all proxies.
     */
    @Override
    public boolean commit() {
        selectionChanged();
        boolean result = super.commit();
        
        if (result) {
            try {
                commitProxies();
            } catch (Exception e) {
                result = false;
                setWrongValueException(e);
                log.error("Error committing changes.", e);
            }
        }
        
        return result;
    }
    
    /**
     * Reverts all changes to the last committed state. This is done by simply discarding all
     * proxies and recreating them from the original child elements.
     */
    @Override
    public boolean revert() {
        boolean result = super.revert();
        
        if (result) {
            resetTree();
        }
        
        return result;
    }
    
    /**
     * Rebuilds the tree from original target, canceling any pending changes.
     */
    private void resetTree() {
        currentItem = null;
        proxies.clear();
        tree.destroyChildren();
        initTree(getTarget(), tree);
        selectNode((Treenode) tree.getFirstChild());
        hasChanged = false;
    }
    
    /**
     * Initializes the tree view based on the children of the specified target.
     * 
     * @param target The target UI element whose children will be added to the tree.
     * @param parent The parent component to receive the new tree items.
     */
    private void initTree(UIElementBase target, BaseComponent parent) {
        for (UIElementBase child : target.getChildren()) {
            if (childClass == null || childClass.isInstance(child)) {
                Treenode node = addTreenode(newProxy(child), parent, null);
                
                if (hierarchical) {
                    initTree(child, node);
                }
            }
        }
    }
    
    private void addTreenode(PluginDefinition def) {
        if (def != null) {
            Proxy proxy = newProxy(def);
            selectNode(addTreenode(proxy, null, tree.getSelectedNode()));
            doChanged(true);
        }
    }
    
    /**
     * Adds a tree node under the specified parent.
     * 
     * @param proxy The proxy associated with the tree node.
     * @param parent The tree parent to receive the new tree node.
     * @param insertAfter Tree node after which new node will be added (may be null).
     * @return The newly created tree node.
     */
    protected Treenode addTreenode(Proxy proxy, BaseComponent parent, Treenode insertAfter) {
        Treenode node = null;
        
        if (proxy != null) {
            node = new Treenode();
            parent = parent == null ? (insertAfter == null ? tree : insertAfter.getParent()) : parent;
            BaseComponent refChild = insertAfter == null ? null : insertAfter.getNextSibling();
            parent.addChild(node, refChild);
            proxy.setItem(node);
            initProxy(proxy);
            
            if (hasLabelProperty(proxy.getDefinition().getClazz())) {
                node.addEventForward(DblclickEvent.TYPE, tree, null);
            }
        }
        
        return node;
    }
    
    /**
     * Returns true if the class has a writable label property.
     * 
     * @param clazz The class to check.
     * @return True if a writable label property exists.
     */
    private boolean hasLabelProperty(Class<?> clazz) {
        try {
            PropertyDescriptor pd = labelProperty == null ? null : BeanUtils.getPropertyDescriptor(clazz, labelProperty);
            return pd != null && pd.getWriteMethod() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Commits changes to all proxies to their proxied elements. For insertion and deletion
     * operations, the proxied elements are created or deleted at this time. The commit operation
     * writes all property settings from the proxy to the proxied element.
     * 
     * @throws Exception Unspecified exception.
     */
    private void commitProxies() throws Exception {
        for (Proxy proxy : proxies) {
            proxy.realize();
            proxy.commit();
        }
        
        resequenceTargets(tree, getTarget());
        hasChanged = false;
    }
    
    /**
     * Resequence all UI elements to match that of the tree view.
     * 
     * @param tc Root of subtree.
     * @param parent The parent UI element.
     */
    protected void resequenceTargets(BaseComponent tc, UIElementBase parent) {
        if (tc != null) {
            int index = -1;
            
            for (BaseComponent child : tc.getChildren()) {
                index++;
                Treenode node = (Treenode) child;
                UIElementBase target = getProxy(node).getTarget();
                target.setParent(parent);
                target.setIndex(index);
                resequenceTargets(node, target);
            }
        }
    }
    
    /**
     * Creates a new proxy for the specified child.
     * 
     * @param child Element to be proxied. May not be null.
     * @return The proxy wrapping the specified child.
     */
    private Proxy newProxy(UIElementBase child) {
        Assert.notNull(child, "Child element may not be null");
        @SuppressWarnings("unchecked")
        Proxy proxy = new Proxy((T) child);
        proxies.add(proxy);
        return proxy;
    }
    
    /**
     * Creates a new proxy for a child to be created.
     * 
     * @param def Plugin definition for child to be created.
     * @return The proxy wrapping the specified plugin definition.
     */
    private Proxy newProxy(PluginDefinition def) {
        Assert.notNull(def, "Plugin definition may not be null");
        Proxy proxy = new Proxy(def);
        proxies.add(proxy);
        return proxy;
    }
    
    /**
     * Occurs when the tree view selection changes.
     */
    @EventHandler(value = "change", target = "@tree")
    private void onSelect$tree() {
        selectionChanged();
    }
    
    /**
     * Double-clicking a tree node allows in place editing.
     * 
     * @param event The double click event.
     */
    @EventHandler(value = "dblclick", target = "@tree")
    private void onDoubleClick$tree(Event event) {
        BaseComponent target = event.getTarget();
        
        if (target instanceof Treenode) {
            event.stopPropagation();
            editNodeStart((Treenode) target);
        }
    }
    
    public void onLabelChange$tree() {
        Object label = labelEditor.getValue();
        updateLabel(currentItem, label == null ? "" : label.toString());
    }
    
    /**
     * Place the specified tree node in edit mode. In this mode, the node's label is replaced with a
     * text box containing the label's value.
     * 
     * @param node Target tree node.
     */
    private void editNodeStart(Treenode node) {
        txtLabel.setAttribute(ITEM_ATTR, node);
        
        if (node == null) {
            popLabel.close();
        } else {
            String label = node.getLabel();
            node.setAttribute(LABEL_ATTR, label);
            txtLabel.setValue(label);
            node.setLabel(null);
            popLabel.open(node, "left top", "left top");
            txtLabel.setFocus(true);
        }
    }
    
    /**
     * Called after a node's label has been edited. Updates the node's label with the edited value
     * and takes the node out of edit mode.
     */
    private void editNodeStop() {
        Treenode currentEdit = (Treenode) txtLabel.getAttribute(ITEM_ATTR);
        txtLabel.removeAttribute(ITEM_ATTR);
        popLabel.close();
        
        if (currentEdit != null) {
            String oldLabel = (String) currentEdit.getAttribute(LABEL_ATTR);
            String newLabel = txtLabel.getValue();
            newLabel = newLabel != null && !newLabel.trim().isEmpty() ? newLabel : oldLabel;
            currentEdit.setLabel(newLabel);
            
            if (!newLabel.equals(oldLabel)) {
                updateLabel(currentEdit, newLabel);
            }
        }
    }
    
    @Override
    public void doClose() {
        tree.focus();
        EventUtil.post(ChangeEvent.TYPE, tree, null); // Must be done asynchronously to allow server to sync with client changes
    }
    
    /**
     * If the property grid is closed, instead close the component.
     * 
     * @param event The close event.
     */
    @EventHandler(value = "close", target = "^")
    private void onClose(Event event) {
        if (event.getTarget().getAttribute("controller") == propertyGrid) {
            editor.close();
            doClose();
        }
    }
    
    /**
     * Returns the proxy associated with the given tree node.
     * 
     * @param node A tree node.
     * @return The proxy associated with the tree node.
     */
    @SuppressWarnings("unchecked")
    private Proxy getProxy(Treenode node) {
        return node == null ? null : (Proxy) node.getData();
    }
    
    /**
     * Call this to update the grid to reflect a newly selected target.
     */
    private void selectionChanged() {
        selectionChanging = true;
        editNodeStop();
        
        if (currentItem != null) {
            propertyGrid.commitChanges(true);
            Proxy proxy = getProxy(currentItem);
            
            if (proxy != null) {
                currentItem.setLabel(proxy.getLabel());
            }
        }
        
        currentItem = tree.getSelectedNode();
        propertyGrid.setTarget(getProxy(currentItem));
        labelEditor = propertyGrid.findEditor(labelProperty, false);
        
        if (labelEditor != null) {
            labelEditor.getEditor().addEventForward(ChangeEvent.TYPE, tree, "onLabelChange");
        }
        
        updateControls();
        selectionChanging = false;
    }
    
    /**
     * Updates the label for this node.
     * 
     * @param node A tree node.
     * @param label Updated label.
     */
    private void updateLabel(Treenode node, String label) {
        Proxy proxy = getProxy(node);
        
        if (proxy != null) {
            proxy.setLabel(label);
        } else {
            node.setLabel(label);
        }
        
        if (node == tree.getSelectedNode() && labelEditor != null) {
            labelEditor.revert();
        }
        
        doChanged(false);
    }
    
    /**
     * Causes a new tree node to be selected and the display state to be updated.
     * 
     * @param node = Item to be selected.
     */
    private void selectNode(Treenode node) {
        tree.setSelectedNode(node);
        selectionChanged();
    }
    
    @EventHandler(value = "close", target = "@popLabel")
    private void onClose$popLabel() {
        this.editNodeStop();
    }
    
    @EventHandler(value = "focusout", target = "@txtLabel")
    private void onBlur$txtLabel() {
        this.editNodeStop();
    }
    
    /**
     * Add a new node.
     */
    @EventHandler(value = "click", target = "btnAdd")
    private void onClick$btnAdd() {
        if (childClass != null) {
            addTreenode(PluginDefinition.getDefinition(childClass));
        } else {
            AddComponent.getDefinition(getTarget(), (def) -> {
                addTreenode(def);
                editor.focus();
                editor.open();
            });
        }
    }
    
    /**
     * Delete the selected node. If the associated proxy does not yet contain a proxied target, it
     * is simply removed from the proxy list. Otherwise, it is marked as deleted, but remains in the
     * list.
     */
    @EventHandler(value = "click", target = "@btnDelete")
    private void onClick$btnDelete() {
        Treenode node = tree.getSelectedNode();
        
        if (node != null) {
            Proxy proxy = getProxy(node);
            
            if (proxy.getTarget() == null) {
                proxies.remove(proxy);
            } else {
                proxy.setDeleted(true);
            }
            
            Treenode nextItem = (Treenode) node.getNextSibling();
            nextItem = nextItem == null ? (Treenode) node.getPreviousSibling() : nextItem;
            node.destroy();
            currentItem = null;
            selectNode(nextItem);
            doChanged(true);
        }
    }
    
    /**
     * Move the selected tree node up one level.
     */
    @EventHandler(value = "click", target = "@btnUp")
    private void onClick$btnUp() {
        Treenode node = tree.getSelectedNode();
        Treenode sib = (Treenode) node.getPreviousSibling();
        swap(sib, node);
    }
    
    /**
     * Move the selected tree node down one level.
     */
    @EventHandler(value = "click", target = "@btnDown")
    private void onClick$btnDown() {
        Treenode node = tree.getSelectedNode();
        Treenode sib = (Treenode) node.getNextSibling();
        swap(node, sib);
    }
    
    /**
     * Promote the selected tree node.
     */
    @EventHandler(value = "click", target = "@btnLeft")
    private void onClick$btnLeft() {
        Treenode node = tree.getSelectedNode();
        Treenode parent = (Treenode) node.getParent();
        parent.getParent().addChild(node, parent.getNextSibling());
        doChanged(true);
    }
    
    /**
     * Demote the selected tree node.
     */
    @EventHandler(value = "click", target = "@btnRight")
    private void onClick$btnRight() {
        Treenode node = tree.getSelectedNode();
        Treenode sib = (Treenode) node.getPreviousSibling();
        sib.addChild(node);
        doChanged(true);
    }
    
    /**
     * Moves item1 to follow item2.
     * 
     * @param item1 = A tree node.
     * @param item2 = A tree node.
     */
    private void swap(Treenode item1, Treenode item2) {
        if (item1 != null && item2 != null) {
            item1.getParent().swapChildren(item1.getIndex(), item2.getIndex());
            doChanged(true);
        }
    }
    
    /**
     * Update the hasChanged flag. Fires an event to the parent property grid if the state has
     * changed.
     * 
     * @param updateControls If true, control states are updated first.
     */
    private void doChanged(boolean updateControls) {
        if (updateControls) {
            updateControls();
        }
        
        if (!selectionChanging && !hasChanged) {
            hasChanged = true;
            propertyGrid.propertyChanged();
        }
    }
    
    /**
     * Override to enable promotion of items
     * 
     * @param node The node to test.
     * @return True if the node can be promoted.
     */
    protected boolean canPromote(Treenode node) {
        return hierarchical && node.getPreviousSibling() != null;
    }
    
    /**
     * Override to enable promotion of items
     * 
     * @param node The node to test.
     * @return True if the node can be demoted.
     */
    protected boolean canDemote(Treenode node) {
        return hierarchical && node.getParent() != tree;
    }
    
    /**
     * Update toolbar buttons to reflect the current selection state.
     */
    private void updateControls() {
        //TODO: TreeUtil.adjustVisibility(tree);
        Treenode node = tree.getSelectedNode();
        disableButton(btnDelete, node == null);
        disableButton(btnRight, node == null || !canPromote(node));
        disableButton(btnLeft, node == null || !canDemote(node));
        disableButton(btnUp, node == null || node.getPreviousSibling() == null);
        disableButton(btnDown, node == null || node.getNextSibling() == null);
    }
    
    private void disableButton(Button btn, boolean disabled) {
        btn.setDisabled(disabled);
        btn.addStyle("opacity", disabled ? ".4" : "1");
    }
    
    /**
     * Returns true if changes have been made since the last commit.
     */
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }
    
    @Override
    protected Object getValue() {
        return null;
    }
    
    @Override
    protected void setValue(Object value) {
    }
    
}
