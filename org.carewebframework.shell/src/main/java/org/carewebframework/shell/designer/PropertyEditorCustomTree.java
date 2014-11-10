/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementProxy;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.zk.TreeUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.beans.BeanUtils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

/**
 * Abstract class for implementing a custom property editor based on a tree view display of child
 * elements that can be manipulated and whose properties may be edited.
 * 
 * @param <T> Type of child element.
 */
public abstract class PropertyEditorCustomTree<T extends UIElementBase> extends PropertyEditorCustom {
    
    private static class LabelConstraint implements Constraint {
        
        private Component lastTarget;
        
        @Override
        public void validate(Component comp, Object value) throws WrongValueException {
            clearMessage();
            String realValue = value == null ? null : ((String) value).trim();
            lastTarget = (Component) comp.getAttribute(ITEM_ATTR);
            
            if (StringUtils.isEmpty(realValue)) {
                throw new WrongValueException(lastTarget, "Label cannot be blank.");
            }
        }
        
        public void clearMessage() {
            if (lastTarget != null) {
                Clients.clearWrongValue(lastTarget);
                lastTarget = null;
            }
        }
    }
    
    /**
     * Subclass UIElementProxy to allow us to synchronize changes to label property with the
     * corresponding tree node.
     */
    private class Proxy extends UIElementProxy {
        
        private Treeitem item;
        
        public Proxy(T child) {
            super(child);
        }
        
        public Proxy(PluginDefinition def) {
            super(def);
        }
        
        public UIElementBase realize() throws Exception {
            Treeitem parentItem = item.getParentItem();
            UIElementBase parentElement = parentItem == null ? getTarget() : getProxy(parentItem).realize();
            realize(parentElement);
            return getTarget();
        }
        
        public void setItem(Treeitem item) {
            this.item = item;
            item.setValue(this);
            item.setLabel(getLabel());
        }
        
        /**
         * Returns the label property value.
         * 
         * @return The label text, or null if none.
         */
        public String getLabel() {
            String label = getProperty(labelProperty);
            label = label == null ? item.getLabel() : label;
            
            if (label == null) {
                label = getDefaultInstanceName();
                setProperty(labelProperty, label);
            }
            
            return label;
        }
        
        public void setLabel(String label) {
            setProperty(labelProperty, label);
            item.setLabel(label);
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
            Tree tree = item.getTree();
            
            while (TreeUtil.findNodeByLabel(tree, name + ++i, false) != null) {}
            return name + i;
        }
        
    }
    
    private static final Log log = LogFactory.getLog(PropertyEditorCustomTree.class);
    
    private static final String ITEM_ATTR = "EDITED_ITEM";
    
    private static final String LABEL_ATTR = "ITEM_LABEL";
    
    private Toolbarbutton btnUp;
    
    private Toolbarbutton btnDown;
    
    private Toolbarbutton btnRight;
    
    private Toolbarbutton btnLeft;
    
    private Toolbarbutton btnDelete;
    
    private Tree tree;
    
    private Treechildren root;
    
    private Component gridParent;
    
    private final PropertyGrid propertyGrid;
    
    private Treeitem currentItem;
    
    private final boolean hierarchical;
    
    private final String labelProperty;
    
    private boolean hasChanged;
    
    private boolean selectionChanging;
    
    private Event changeEvent;
    
    private final Textbox txtLabel = new Textbox();
    
    private PropertyEditorBase labelEditor;
    
    private final List<Proxy> proxies = new ArrayList<Proxy>();
    
    private final Class<T> childClass;
    
    private final PluginDefinition definition;
    
    /**
     * Create the editor instance.
     * 
     * @param childClass This is the class of child elements managed by this editor. It is used to
     *            create new instances of the UI element from within the editor and to restrict the
     *            selection of child elements to that type only.
     *            <p>
     *            The owner class may be null. In this case, there is no restriction on the child
     *            type. The user is prompted for an element type when creating a new instance.
     * @param labelProperty This is the name of the property on the child UI element that will be
     *            synchronized with the label of the corresponding tree node. It may be null.
     * @param hierarchical If true, the editor assumes that child elements may contain other child
     *            elements.
     * @throws Exception Unspecified exception.
     */
    public PropertyEditorCustomTree(Class<T> childClass, String labelProperty, boolean hierarchical) throws Exception {
        super(DesignConstants.RESOURCE_PREFIX + "PropertyEditorCustomTree.zul");
        this.childClass = childClass;
        this.labelProperty = labelProperty;
        this.hierarchical = hierarchical;
        btnRight.setVisible(hierarchical);
        btnLeft.setVisible(hierarchical);
        propertyGrid = PropertyGrid.create(null, gridParent);
        propertyGrid.setClosable(true);
        Events.addEventListeners(propertyGrid, this);
        bandpopup.setWidth("600px");
        bandpopup.setHeight("400px");
        txtLabel.setWidth("95%");
        txtLabel.setConstraint(new LabelConstraint());
        definition = childClass == null ? null : PluginRegistry.getInstance().get(childClass);
        
        EventListener<Event> labelEditorListener = new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                event.stopPropagation();
                editNodeStop();
            }
        };
        
        txtLabel.addEventListener(Events.ON_CHANGE, labelEditorListener);
        txtLabel.addEventListener(Events.ON_BLUR, labelEditorListener);
        txtLabel.addEventListener(Events.ON_OK, labelEditorListener);
    }
    
    /**
     * Returns the plugin definition for the proxied object. This is used to for cases where a proxy
     * is created for a child element that does not yet exist. It enables the proxy to defer
     * creation of the proxied element until a commit occurs.
     * <p>
     * Note that if there is no specified plugin definition, the add component dialog will be
     * presented, allowing the user to select which definition to use.
     * 
     * @return The plugin definition to use. This may be null.
     */
    protected PluginDefinition getTargetDefinition() {
        if (definition != null) {
            return definition;
        }
        
        PluginDefinition def = AddComponent.getDefinition(getTarget());
        bandbox.open();
        return def;
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
        changeEvent = new Event(Events.ON_CHANGE, propGrid);
        resetTree();
    }
    
    /**
     * Commits changes to all proxies.
     */
    @Override
    public boolean commit() {
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
        ZKUtil.detachChildren(root);
        initTree(getTarget(), root);
        selectItem((Treeitem) root.getFirstChild());
        hasChanged = false;
    }
    
    /**
     * Initializes the tree view based on the children of the specified target.
     * 
     * @param target The target UI element whose children will be added to the tree.
     * @param parent The parent component to receive the new tree items.
     */
    private void initTree(UIElementBase target, Component parent) {
        for (UIElementBase child : target.getChildren()) {
            if (childClass == null || childClass.isInstance(child)) {
                @SuppressWarnings("unchecked")
                Treeitem item = addTreeitem((T) child, parent, null);
                
                if (hierarchical) {
                    initTree(child, getTreechildren(item));
                }
            }
        }
    }
    
    /**
     * Returns the tree children for the item, creating one if it does not exist.
     * 
     * @param item Item whose tree children property is sought.
     * @return Tree children for item.
     */
    private Treechildren getTreechildren(Treeitem item) {
        Treechildren tc = item.getTreechildren();
        
        if (tc == null) {
            item.appendChild(tc = new Treechildren());
        }
        
        return tc;
    }
    
    /**
     * Adds a tree item under the specified parent.
     * 
     * @param child Child to be associated (via a proxy) with the newly created tree item.
     * @param parent The tree parent to receive the new tree item.
     * @param insertAfter Tree item after which new item will be added (may be null).
     * @return The newly created tree item.
     */
    protected Treeitem addTreeitem(T child, Component parent, Treeitem insertAfter) {
        Proxy proxy = newProxy(child);
        Treeitem item = null;
        
        if (proxy != null) {
            item = new Treeitem();
            parent = parent == null ? (insertAfter == null ? root : insertAfter.getParent()) : parent;
            Component refChild = insertAfter == null ? null : insertAfter.getNextSibling();
            parent.insertBefore(item, refChild);
            proxy.setItem(item);
            initProxy(proxy);
            
            if (hasLabelProperty(proxy.getDefinition().getClazz())) {
                item.addForward(Events.ON_DOUBLE_CLICK, tree, null);
            }
        }
        
        return item;
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
        
        resequenceTargets(root, getTarget());
        hasChanged = false;
    }
    
    /**
     * Resequence all UI elements to match that of the tree view.
     * 
     * @param tc Root of subtree.
     * @param parent The parent UI element.
     */
    protected void resequenceTargets(Treechildren tc, UIElementBase parent) {
        if (tc != null) {
            int index = -1;
            
            for (Component child : tc.getChildren()) {
                index++;
                Treeitem item = (Treeitem) child;
                UIElementBase target = getProxy(item).getTarget();
                target.setParent(parent);
                target.setIndex(index);
                resequenceTargets(item.getTreechildren(), target);
            }
        }
    }
    
    /**
     * Creates a new proxy for the specified child. If the child is null, a proxy is created for the
     * child class.
     * 
     * @param child Element to be proxied. May be null.
     * @return The proxy wrapping the specified child.
     */
    private Proxy newProxy(T child) {
        Proxy proxy;
        
        if (child != null) {
            proxy = new Proxy(child);
        } else {
            PluginDefinition def = getTargetDefinition();
            
            if (def == null) {
                return null;
            }
            
            proxy = new Proxy(def);
        }
        
        proxies.add(proxy);
        return proxy;
    }
    
    /**
     * Occurs when the tree view selection changes.
     */
    public void onSelect$tree() {
        selectionChanged();
    }
    
    /**
     * Double-clicking a tree item allows in place editing.
     * 
     * @param event The double click event.
     */
    public void onDoubleClick$tree(Event event) {
        Component target = ZKUtil.getEventOrigin(event).getTarget();
        
        if (target instanceof Treeitem) {
            event.stopPropagation();
            editNodeStart((Treeitem) target);
        }
    }
    
    public void onLabelChange$tree() {
        Object label = labelEditor.getValue();
        updateLabel(currentItem, label == null ? "" : label.toString());
    }
    
    /**
     * Place the specified tree item in edit mode. In this mode, the node's label is replaced with a
     * text box containing the label's value.
     * 
     * @param item Target tree item.
     */
    private void editNodeStart(Treeitem item) {
        txtLabel.setAttribute(ITEM_ATTR, item);
        
        if (item == null) {
            txtLabel.detach();
        } else {
            String label = item.getLabel();
            item.setAttribute(LABEL_ATTR, label);
            txtLabel.setValue(label);
            item.setLabel(null);
            item.getTreerow().getFirstChild().appendChild(txtLabel);
            txtLabel.setFocus(true);
        }
    }
    
    /**
     * Called after a node's label has been edited. Updates the node's label with the edited value
     * and takes the node out of edit mode.
     */
    private void editNodeStop() {
        Treeitem currentEdit = (Treeitem) txtLabel.getAttribute(ITEM_ATTR);
        txtLabel.removeAttribute(ITEM_ATTR);
        txtLabel.detach();
        
        if (currentEdit != null) {
            String oldLabel = (String) currentEdit.getAttribute(LABEL_ATTR);
            String newLabel = txtLabel.isValid() ? txtLabel.getValue() : oldLabel;
            currentEdit.setLabel(newLabel);
            
            if (!newLabel.equals(oldLabel)) {
                updateLabel(currentEdit, newLabel);
            }
        }
    }
    
    @Override
    public void doClose() {
        tree.focus();
        Events.echoEvent(Events.ON_SELECT, tree, null); // Must be done asynchronously to allow server to sync with client changes
    }
    
    /**
     * If the property grid is closed, instead close the bandbox.
     * 
     * @param event The close event.
     */
    public void onClose(Event event) {
        if (event.getTarget() == propertyGrid) {
            bandbox.close();
            doClose();
        }
    }
    
    /**
     * Returns the proxy associated with the given tree item.
     * 
     * @param item A tree item.
     * @return The proxy associated with the tree item.
     */
    @SuppressWarnings("unchecked")
    private Proxy getProxy(Treeitem item) {
        return item == null ? null : (Proxy) item.getValue();
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
        
        currentItem = tree.getSelectedItem();
        propertyGrid.setTarget(getProxy(currentItem));
        labelEditor = propertyGrid.findEditor(labelProperty);
        
        if (labelEditor != null) {
            labelEditor.getComponent().addForward(Events.ON_CHANGE, tree, "onLabelChange");
        }
        
        updateControls();
        selectionChanging = false;
    }
    
    /**
     * Updates the label for this node.
     * 
     * @param item A tree item.
     * @param label Updated label.
     */
    private void updateLabel(Treeitem item, String label) {
        Proxy proxy = getProxy(item);
        
        if (proxy != null) {
            proxy.setLabel(label);
        } else {
            item.setLabel(label);
        }
        
        if (item == tree.getSelectedItem() && labelEditor != null) {
            labelEditor.revert();
        }
        
        doChanged(false);
    }
    
    /**
     * Causes a new tree item to be selected and the display state to be updated.
     * 
     * @param item = Item to be selected.
     */
    private void selectItem(Treeitem item) {
        tree.setSelectedItem(item);
        selectionChanged();
    }
    
    /**
     * Add a new item.
     */
    public void onClick$btnAdd() {
        selectItem(addTreeitem(null, null, tree.getSelectedItem()));
        doChanged(true);
    }
    
    /**
     * Delete the selected item. If the associated proxy does not yet contain a proxied target, it
     * is simply removed from the proxy list. Otherwise, it is marked as deleted, but remains in the
     * list.
     */
    public void onClick$btnDelete() {
        Treeitem item = tree.getSelectedItem();
        
        if (item != null) {
            Proxy proxy = getProxy(item);
            
            if (proxy.getTarget() == null) {
                proxies.remove(proxy);
            } else {
                proxy.setDeleted(true);
            }
            
            Treeitem nextItem = (Treeitem) item.getNextSibling();
            nextItem = nextItem == null ? (Treeitem) item.getPreviousSibling() : nextItem;
            item.detach();
            currentItem = null;
            selectItem(nextItem);
            doChanged(true);
        }
    }
    
    /**
     * Move the selected tree item up one level.
     */
    public void onClick$btnUp() {
        Treeitem item = tree.getSelectedItem();
        Treeitem sib = (Treeitem) item.getPreviousSibling();
        swap(sib, item);
    }
    
    /**
     * Move the selected tree item down one level.
     */
    public void onClick$btnDown() {
        Treeitem item = tree.getSelectedItem();
        Treeitem sib = (Treeitem) item.getNextSibling();
        swap(item, sib);
    }
    
    /**
     * Promote the selected tree item.
     */
    public void onClick$btnLeft() {
        Treeitem item = tree.getSelectedItem();
        Treeitem parent = item.getParentItem();
        parent.getParent().insertBefore(item, parent.getNextSibling());
        doChanged(true);
        updateControls();
    }
    
    /**
     * Demote the selected tree item.
     */
    public void onClick$btnRight() {
        Treeitem item = tree.getSelectedItem();
        Treeitem sib = (Treeitem) item.getPreviousSibling();
        Treechildren treechildren = getTreechildren(sib);
        treechildren.appendChild(item);
        doChanged(true);
        updateControls();
    }
    
    /**
     * Moves item1 to follow item2.
     * 
     * @param item1 = A tree item.
     * @param item2 = A tree item.
     */
    private void swap(Treeitem item1, Treeitem item2) {
        if (item1 != null && item2 != null) {
            Component parent = item2.getParent();
            item2.detach();
            parent.insertBefore(item2, item1);
            doChanged(true);
        }
    }
    
    /**
     * Catches onChange events from the grid.
     * 
     * @param event The onChange event.
     */
    public void onChange(Event event) {
        doChanged(false);
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
            Events.sendEvent(changeEvent);
        }
    }
    
    /**
     * Override to enable promotion of items
     * 
     * @param item The item to test.
     * @return True if the item can be promoted.
     */
    protected boolean canPromote(Treeitem item) {
        return hierarchical && item.getPreviousSibling() != null;
    }
    
    /**
     * Override to enable promotion of items
     * 
     * @param item The item to test.
     * @return True if the item can be demoted.
     */
    protected boolean canDemote(Treeitem item) {
        return hierarchical && item.getParent() != root;
    }
    
    /**
     * Update toolbar buttons to reflect the current selection state.
     */
    private void updateControls() {
        TreeUtil.adjustVisibility(tree);
        Treeitem item = tree.getSelectedItem();
        disableButton(btnDelete, item == null);
        disableButton(btnRight, item == null || !canPromote(item));
        disableButton(btnLeft, item == null || !canDemote(item));
        disableButton(btnUp, item == null || item.getPreviousSibling() == null);
        disableButton(btnDown, item == null || item.getNextSibling() == null);
    }
    
    private void disableButton(Toolbarbutton btn, boolean disabled) {
        btn.setDisabled(disabled);
        btn.setStyle("opacity:" + (disabled ? ".2" : "1"));
    }
    
    /**
     * Returns true if changes have been made since the last commit.
     */
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }
}
