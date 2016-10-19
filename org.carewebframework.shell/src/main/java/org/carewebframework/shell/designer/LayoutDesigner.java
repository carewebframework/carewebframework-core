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

import java.awt.Desktop;
import java.util.Collection;

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.event.DropEvent;
import org.carewebframework.web.event.EventUtil;

/**
 * Dialog for managing the current layout.
 */
public class LayoutDesigner extends Window implements IAutoWired {
    
    private static final String ZUL_PAGE = DesignConstants.RESOURCE_PREFIX + "LayoutDesigner.cwf";
    
    private static final String ATTR_BRING_TO_FRONT = ZUL_PAGE + ".BTF";
    
    private enum MovementType {
        INVALID, // Invalid movement type
        EXCHANGE, // Exchange position of two siblings
        FIRST, // Move ahead of all siblings
        RELOCATE // Move to a different parent
    }
    
    private Treeview tree;
    
    private UIElementBase rootElement;
    
    private Button btnCut;
    
    private Button btnCopy;
    
    private Button btnPaste;
    
    private Button btnAdd;
    
    private Button btnDelete;
    
    private Button btnUp;
    
    private Button btnDown;
    
    private Button btnLeft;
    
    private Button btnRight;
    
    private Button btnToFront;
    
    private Button btnProperties;
    
    private Button btnAbout;
    
    private final Clipboard clipboard = Clipboard.getInstance();
    
    private final DesignContextMenu contextMenu = DesignContextMenu.create();
    
    private int dragId;
    
    private LayoutChangedEvent layoutChangedEvent;
    
    private boolean refreshPending;
    
    private boolean bringToFront;
    
    /**
     * Display the Layout Manager dialog
     * 
     * @param rootElement The root UI element.
     * @throws Exception Unspecified exception.
     */
    public static void execute(UIElementBase rootElement) throws Exception {
        LayoutDesigner dlg = getInstance(true);
        dlg.init(rootElement);
        dlg.doOverlapped();
    }
    
    /**
     * Close the dialog if it is open.
     */
    public static void closeDialog() {
        try {
            LayoutDesigner dlg = getInstance(false);
            
            if (dlg != null) {
                dlg.close();
            }
        } catch (Exception e) {}
    }
    
    /**
     * Returns an instance of the layout manager. If the layout manager is open, returns that
     * instance. If not and autoCreate is true, creates a new one.
     * 
     * @param autoCreate If true and dialog does not exist, it is created.
     * @return The layout manager.
     * @throws Exception Unspecified exception.
     */
    private static LayoutDesigner getInstance(boolean autoCreate) throws Exception {
        Page page = ExecutionContext.getPage();
        LayoutDesigner dlg = (LayoutDesigner) page.getAttribute(ZUL_PAGE);
        
        if (autoCreate && dlg == null) {
            dlg = (LayoutDesigner) PopupDialog.popup(ZKUtil.loadCachedPageDefinition(ZUL_PAGE), null, true, true, false);
            page.setAttribute(ZUL_PAGE, dlg);
        }
        
        return dlg;
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        ZKUtil.wireController(this);
        setWidgetOverride("_cwf_highlight", "function(comp) {jq(comp).effect('pulsate',{times:1}).effect('highlight');}");
        Boolean btf = (Boolean) getDesktop().getAttribute(ATTR_BRING_TO_FRONT);
        bringToFront = btf == null || btf;
        layoutChangedEvent = new LayoutChangedEvent(this, null);
        contextMenu.setParent(this);
        contextMenu.setListener(this);
        clipboard.addListener(this);
        getDesktop().addListener(layoutListener);
    }
    
    /**
     * Initialize the tree view based on the current layout.
     * 
     * @param rootElement The root UI element.
     */
    private void init(UIElementBase rootElement) {
        if (this.rootElement != rootElement) {
            this.rootElement = rootElement;
            refresh();
        }
    }
    
    /**
     * Highlights a component.
     * 
     * @param comp Component to highlight.
     */
    private void highlight(BaseComponent comp) {
        response(new AuInvoke(this, "_cwf_highlight", comp));
    }
    
    /**
     * Submits an asynchronous refresh request if one is not already pending.
     */
    public void requestRefresh() {
        if (!refreshPending) {
            refreshPending = true;
            EventUtil.post(layoutChangedEvent);
        }
    }
    
    /**
     * Refreshes the component tree.
     */
    private void refresh() {
        refreshPending = false;
        UIElementBase selectedElement = selectedElement();
        ZKUtil.detachChildren(tree.getTreechildren());
        dragId = 0;
        buildTree(rootElement, null, selectedElement);
        updateDroppable();
        updateControls();
    }
    
    /**
     * Refresh a subtree of the component tree. Called recursively.
     * 
     * @param root Root UI element of the subtree.
     * @param parentNode Tree item that will be the parent node of the subtree.
     * @param selectedElement The currently selected element.
     */
    private void buildTree(UIElementBase root, Treenode parentNode, UIElementBase selectedElement) {
        Treechildren treechildren = parentNode == null ? tree.getTreechildren() : getTreechildren(parentNode);
        Treenode item = createItem(root);
        treechildren.appendChild(item);
        
        if (root == selectedElement) {
            tree.selectItem(item);
        }
        
        for (UIElementBase child : root.getChildren()) {
            buildTree(child, item, selectedElement);
        }
    }
    
    /**
     * Creates a tree item associated with the specified UI element.
     * 
     * @param ele UI element
     * @return Newly created tree item.
     */
    private Treenode createItem(UIElementBase ele) {
        String label = ele.getDisplayName();
        String instanceName = ele.getInstanceName();
        
        if (!label.equalsIgnoreCase(instanceName)) {
            label += " - " + instanceName;
        }
        
        Treenode item = new Treenode();
        item.setData(ele);
        Treerow row = new Treerow(label);
        row.setParent(item);
        row.addForward(Events.ON_DROP, this, null);
        row.addForward(Events.ON_DOUBLE_CLICK, btnProperties, Events.ON_CLICK);
        
        if (!ele.isLocked() && !ele.getDefinition().isInternal()) {
            row.setDraggable("d" + dragId++);
        }
        
        return item;
    }
    
    /**
     * Returns the tree item containing the component.
     * 
     * @param cmpt The component whose containing tree item is sought.
     * @return The tree item.
     */
    private Treenode getTreenode(BaseComponent cmpt) {
        return (Treenode) (cmpt instanceof Treenode ? cmpt : cmpt.getAncestor(Treenode.class));
    }
    
    /**
     * Returns currently selected UI element, or null if none selected.
     * 
     * @return Currently selected item (may be null).
     */
    private UIElementBase selectedElement() {
        return getElement(tree.getSelectedNode());
    }
    
    /**
     * Returns the UI element associated with the given tree item.
     * 
     * @param item A tree item.
     * @return The UI element associated with the tree item.
     */
    private UIElementBase getElement(Treenode item) {
        return (UIElementBase) (item == null ? null : item.getData());
    }
    
    /**
     * Update control states for current selection.
     */
    private void updateControls() {
        Treenode selectedItem = tree.getSelectedNode();
        UIElementBase selectedElement = getElement(selectedItem);
        DesignContextMenu.updateStates(selectedElement, btnAdd, btnDelete, btnCopy, btnCut, btnPaste, btnProperties,
            btnAbout);
        Treenode target = selectedItem == null ? null : selectedItem.getParentItem();
        target = target == null ? null : target.getParentItem();
        btnLeft.setDisabled(movementType(selectedItem, target, false) == MovementType.INVALID);
        target = selectedItem == null ? null : (Treenode) selectedItem.getPreviousSibling();
        btnRight.setDisabled(movementType(selectedItem, target, false) == MovementType.INVALID);
        btnUp.setDisabled(movementType(selectedItem, target, true) == MovementType.INVALID);
        target = selectedItem == null ? null : (Treenode) selectedItem.getNextSibling();
        btnDown.setDisabled(movementType(selectedItem, target, true) == MovementType.INVALID);
        btnToFront.addStyle("opacity", bringToFront ? null : "0.5");
        
        if (selectedElement == null) {
            ZKUtil.suppressContextMenu(this);
        } else {
            setContext(contextMenu);
            contextMenu.setOwner(selectedElement);
        }
        
        if (selectedItem != null) {
            selectedItem.setSelected(false);
            selectedItem.setSelected(true);
        }
    }
    
    /**
     * Returns the type of movement being requested.
     * 
     * @param child The node to be moved.
     * @param target The proposed target.
     * @param allowExchange If true and child and target are siblings, this is an exchange.
     *            Otherwise, it will be evaluated as a potential relocation.
     * @return The movement type.
     */
    private MovementType movementType(Treenode child, Treenode target, boolean allowExchange) {
        if (!canMove(child) || target == null) {
            return MovementType.INVALID;
        }
        
        UIElementBase eleChild = getElement(child);
        UIElementBase eleTarget = getElement(target);
        
        if (eleChild == eleTarget) {
            return MovementType.INVALID;
        }
        
        if (eleChild.getParent() == eleTarget.getParent() && allowExchange) {
            return canMove(target) ? MovementType.EXCHANGE : MovementType.INVALID;
        }
        
        if (eleChild.getParent() == eleTarget) {
            return MovementType.FIRST;
        }
        
        if (eleTarget.canAcceptChild(eleChild) && eleChild.canAcceptParent(eleTarget)) {
            return MovementType.RELOCATE;
        }
        
        return MovementType.INVALID;
    }
    
    private boolean canMove(Treenode item) {
        return item != null && !"false".equals(item.getTreerow().getDraggable());
    }
    
    /**
     * Updates the drop ids for all tree items.
     */
    private void updateDroppable() {
        Iterable<Treenode> items = tree.getChildren(Treenode.class);
        
        for (Treenode item : items) {
            updateDroppable(item, items);
        }
    }
    
    /**
     * Update the drop id for the specified target.
     * 
     * @param target Target tree item.
     * @param items Item list.
     */
    private void updateDroppable(Treenode target, Collection<Treenode> items) {
        StringBuilder sb = new StringBuilder();
        
        if (canMove(target)) {
            for (Treenode dragged : items) {
                if (movementType(dragged, target, true) != MovementType.INVALID) {
                    String id = dragged.getDragid();
                    sb.append(sb.length() > 0 ? "," : "").append(id);
                }
            }
        }
        
        target.setDropid(sb.toString());
    }
    
    /**
     * Refreshes tree when layout has changed.
     */
    public void onLayoutChanged() {
        refresh();
    }
    
    /**
     * Updates tool bar controls when selected changes.
     */
    public void onSelect$tree() {
        UIElementBase ele = selectedElement();
        Object obj = ele == null ? null : ele.getOuterComponent();
        
        if (bringToFront && ele != null) {
            ele.bringToFront();
        }
        
        if (obj instanceof BaseComponent) {
            highlight((BaseComponent) obj);
        }
        updateControls();
    }
    
    /**
     * Performs a cut operation on the selected item.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnCut() throws Exception {
        onClick$btnCopy();
        onClick$btnDelete();
    }
    
    /**
     * Performs a copy operation on the selected item.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnCopy() throws Exception {
        clipboard.copy(UILayout.serialize(selectedElement()));
    }
    
    /**
     * Performs a paste operation, inserted the pasted elements under the current selection.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnPaste() throws Exception {
        Object data = clipboard.getData();
        
        if (data instanceof UILayout) {
            ((UILayout) data).deserialize(selectedElement());
            requestRefresh();
        }
    }
    
    /**
     * Shows clipboard contents.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnView() throws Exception {
        clipboard.view();
    }
    
    /**
     * Refreshes the tree view.
     */
    public void onClick$btnRefresh() {
        refresh();
    }
    
    public void onClick$btnToFront() {
        bringToFront = !bringToFront;
        updateControls();
    }
    
    /**
     * Displays the property grid for the currently selected item.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnProperties() throws Exception {
        if (!btnProperties.isDisabled()) {
            PropertyGrid.create(selectedElement(), null);
        }
    }
    
    /**
     * Invokes the add component dialog. Any newly added component will be placed under the current
     * selection.
     */
    public void onClick$btnAdd() {
        if (AddComponent.newChild(selectedElement()) != null) {
            requestRefresh();
        }
    }
    
    /**
     * Removes the currently selected element and any children.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnDelete() throws Exception {
        UIElementBase child = selectedElement();
        UIElementBase parent = child.getParent();
        parent.removeChild(child, true);
        tree.getSelectedNode().detach();
        updateDroppable();
        updateControls();
    }
    
    public void onClick$btnUp() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, (Treenode) item.getPreviousSibling(), true);
    }
    
    public void onClick$btnDown() {
        Treenode item = tree.getSelectedNode();
        doDrop((Treenode) item.getNextSibling(), item, true);
    }
    
    public void onClick$btnRight() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, (Treenode) item.getPreviousSibling(), false);
    }
    
    public void onClick$btnLeft() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, item.getParentItem().getParentItem(), false);
    }
    
    /**
     * Display the About dialog for the selected element.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnAbout() throws Exception {
        selectedElement().about();
    }
    
    /**
     * Invoked when the clipboard contents changes.
     */
    public void onClipboardChange() {
        updateControls();
    }
    
    /**
     * Handles drop events.
     * 
     * @param event The drop event.
     */
    public void onDrop(DropEvent event) {
        Treenode target = getTreenode(event.getTarget());
        Treenode dragged = getTreenode(event.getRelatedTarget());
        doDrop(dragged, target, true);
    }
    
    private void doDrop(Treenode dragged, Treenode target, boolean allowExchange) {
        UIElementBase eleTarget = getElement(target);
        UIElementBase eleDragged = getElement(dragged);
        
        switch (movementType(dragged, target, allowExchange)) {
            case INVALID:
                return;
            
            case EXCHANGE:
                eleDragged.setIndex(eleTarget.getIndex());
                target.getParent().insertChild(dragged, target);
                break;
            
            case FIRST:
                eleDragged.setIndex(0);
                Treechildren tc = target.getTreechildren();
                tc.insertBefore(dragged, tc.getFirstChild());
                break;
            
            case RELOCATE:
                eleDragged.setParent(eleTarget);
                getTreechildren(target).appendChild(dragged);
                break;
        }
        updateDroppable();
        updateControls();
    }
    
    /**
     * Remove all listeners upon close.
     */
    @Override
    public void close() {
        Desktop desktop = getDesktop();
        desktop.removeListener(layoutListener);
        desktop.removeAttribute(ZUL_PAGE);
        desktop.setAttribute(ATTR_BRING_TO_FRONT, bringToFront);
        clipboard.removeListener(this);
        super.close();
    }
}
