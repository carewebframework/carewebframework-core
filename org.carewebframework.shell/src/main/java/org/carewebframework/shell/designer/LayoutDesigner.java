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

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.component.Window.CloseAction;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.DropEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;

/**
 * Controller for dialog for managing the current layout.
 */
public class LayoutDesigner implements IAutoWired {
    
    private static final String CWF_PAGE = DesignConstants.RESOURCE_PREFIX + "layoutDesigner.cwf";
    
    private static final String ATTR_BRING_TO_FRONT = CWF_PAGE + ".BTF";
    
    private enum MovementType {
        INVALID, // Invalid movement type
        EXCHANGE, // Exchange position of two siblings
        FIRST, // Move ahead of all siblings
        RELOCATE // Move to a different parent
    }
    
    private UIElementBase rootElement;
    
    private Window window;
    
    @WiredComponent
    private Treeview tree;
    
    @WiredComponent
    private Button btnCut;
    
    @WiredComponent
    private Button btnCopy;
    
    @WiredComponent
    private Button btnPaste;
    
    @WiredComponent
    private Button btnAdd;
    
    @WiredComponent
    private Button btnDelete;
    
    @WiredComponent
    private Button btnUp;
    
    @WiredComponent
    private Button btnDown;
    
    @WiredComponent
    private Button btnLeft;
    
    @WiredComponent
    private Button btnRight;
    
    @WiredComponent
    private Button btnToFront;
    
    @WiredComponent
    private Button btnProperties;
    
    @WiredComponent
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
     */
    public static void execute(UIElementBase rootElement) {
        Window dlg = getInstance(true);
        dlg.getAttribute("controller", LayoutDesigner.class).init(rootElement);
        dlg.popup(null);
    }
    
    /**
     * Close the dialog if it is open.
     */
    public static void closeDialog() {
        Window dlg = getInstance(false);
        
        if (dlg != null) {
            dlg.close();
        }
    }
    
    /**
     * Returns an instance of the layout manager. If the layout manager is open, returns that
     * instance. If not and autoCreate is true, creates a new one.
     * 
     * @param autoCreate If true and dialog does not exist, it is created.
     * @return The layout manager.
     */
    private static Window getInstance(boolean autoCreate) {
        Page page = ExecutionContext.getPage();
        Window dlg = page.getAttribute(CWF_PAGE, Window.class);
        
        if (autoCreate && dlg == null) {
            dlg = DialogUtil.popup(CWF_PAGE, true, true, false);
            page.setAttribute(CWF_PAGE, dlg);
        }
        
        return dlg;
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        //TODO: setWidgetOverride("_cwf_highlight", "function(comp) {jq(comp).effect('pulsate',{times:1}).effect('highlight');}");
        window = (Window) comp;
        window.setCloseAction(CloseAction.HIDE);
        comp.setAttribute("controller", this);
        bringToFront = comp.getPage().getAttribute(ATTR_BRING_TO_FRONT, true);
        layoutChangedEvent = new LayoutChangedEvent(comp, null);
        contextMenu.setListener(comp);
        clipboard.addListener(comp);
        comp.getPage().addEventListener("register unregister", (event) -> {
            //refresh();
        });
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
        //TODO: response(new AuInvoke(this, "_cwf_highlight", comp));
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
        tree.destroyChildren();
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
        Treenode item = createItem(root);
        item.setParent(parentNode == null ? tree : parentNode);
        
        if (root == selectedElement) {
            tree.setSelectedNode(item);
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
        item.setLabel(label);
        item.setData(ele);
        item.addEventForward(DropEvent.class, window, null);
        item.addEventForward(DblclickEvent.class, btnProperties, ClickEvent.TYPE);
        
        if (!ele.isLocked() && !ele.getDefinition().isInternal()) {
            item.setDragid("d" + dragId++);
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
        return (UIElementBase) (item == null ? rootElement : item.getData());
    }
    
    /**
     * Update control states for current selection.
     */
    private void updateControls() {
        Treenode selectedItem = tree.getSelectedNode();
        UIElementBase selectedElement = getElement(selectedItem);
        contextMenu.updateStates(selectedElement, btnAdd, btnDelete, btnCopy, btnCut, btnPaste, btnProperties, btnAbout);
        BaseComponent parent = selectedItem == null ? null : selectedItem.getParent();
        parent = parent == null ? null : parent.getParent();
        Treenode target = parent instanceof Treenode ? (Treenode) parent : null;
        btnLeft.setDisabled(movementType(selectedItem, target, false) == MovementType.INVALID);
        target = selectedItem == null ? null : (Treenode) selectedItem.getPreviousSibling();
        btnRight.setDisabled(movementType(selectedItem, target, false) == MovementType.INVALID);
        btnUp.setDisabled(movementType(selectedItem, target, true) == MovementType.INVALID);
        target = selectedItem == null ? null : (Treenode) selectedItem.getNextSibling();
        btnDown.setDisabled(movementType(selectedItem, target, true) == MovementType.INVALID);
        btnToFront.addStyle("opacity", bringToFront ? null : "0.5");
        
        if (selectedElement != null) {
            window.setContext(contextMenu.getMenupopup());
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
        return item != null && item.getDragid() != null;
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
    private void updateDroppable(Treenode target, Iterable<Treenode> items) {
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
    private void onLayoutChanged() {
        refresh();
    }
    
    /**
     * Updates tool bar controls when selected changes.
     */
    @EventHandler(value = "select", target = "@tree")
    private void onSelect$tree() {
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
     */
    @EventHandler(value = "click", target = "@btnCut")
    private void onClick$btnCut() {
        onClick$btnCopy();
        onClick$btnDelete();
    }
    
    /**
     * Performs a copy operation on the selected item.
     */
    @EventHandler(value = "click", target = "@btnCopy")
    private void onClick$btnCopy() {
        clipboard.copy(UILayout.serialize(selectedElement()));
    }
    
    /**
     * Performs a paste operation, inserted the pasted elements under the current selection.
     */
    @EventHandler(value = "click", target = "@btnPaste")
    private void onClick$btnPaste() {
        Object data = clipboard.getData();
        
        if (data instanceof UILayout) {
            ((UILayout) data).deserialize(selectedElement());
            requestRefresh();
        }
    }
    
    /**
     * Shows clipboard contents.
     */
    @EventHandler(value = "click", target = "btnView")
    private void onClick$btnView() {
        clipboard.view();
    }
    
    /**
     * Refreshes the tree view.
     */
    @EventHandler(value = "click", target = "btnRefresh")
    private void onClick$btnRefresh() {
        refresh();
    }
    
    @EventHandler(value = "click", target = "@btnToFront")
    private void onClick$btnToFront() {
        bringToFront = !bringToFront;
        updateControls();
    }
    
    /**
     * Displays the property grid for the currently selected item.
     */
    @EventHandler(value = "click", target = "@btnProperties")
    private void onClick$btnProperties() {
        if (!btnProperties.isDisabled()) {
            PropertyGrid.create(selectedElement(), null);
        }
    }
    
    /**
     * Invokes the add component dialog. Any newly added component will be placed under the current
     * selection.
     */
    @EventHandler(value = "click", target = "@btnAdd")
    private void onClick$btnAdd() {
        if (AddComponent.newChild(selectedElement()) != null) {
            requestRefresh();
        }
    }
    
    /**
     * Removes the currently selected element and any children.
     */
    @EventHandler(value = "click", target = "@btnDelete")
    private void onClick$btnDelete() {
        UIElementBase child = selectedElement();
        UIElementBase parent = child.getParent();
        parent.removeChild(child, true);
        tree.getSelectedNode().destroy();
        updateDroppable();
        updateControls();
    }
    
    @EventHandler(value = "click", target = "@btnUp")
    private void onClick$btnUp() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, (Treenode) item.getPreviousSibling(), true);
    }
    
    @EventHandler(value = "click", target = "@btnDown")
    private void onClick$btnDown() {
        Treenode item = tree.getSelectedNode();
        doDrop((Treenode) item.getNextSibling(), item, true);
    }
    
    @EventHandler(value = "click", target = "@btnRight")
    private void onClick$btnRight() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, (Treenode) item.getPreviousSibling(), false);
    }
    
    @EventHandler(value = "click", target = "@btnLeft")
    private void onClick$btnLeft() {
        Treenode item = tree.getSelectedNode();
        doDrop(item, (Treenode) item.getParent().getParent(), false);
    }
    
    /**
     * Display the About dialog for the selected element.
     */
    @EventHandler(value = "click", target = "@btnAbout")
    private void onClick$btnAbout() {
        selectedElement().about();
    }
    
    /**
     * Invoked when the clipboard contents changes.
     */
    private void onClipboardChange() {
        updateControls();
    }
    
    /**
     * Handles drop events.
     * 
     * @param event The drop event.
     */
    private void onDrop(DropEvent event) {
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
                target.getParent().addChild(dragged, target);
                break;
            
            case FIRST:
                eleDragged.setIndex(0);
                target.addChild(dragged, 0);
                break;
            
            case RELOCATE:
                eleDragged.setParent(eleTarget);
                getTreenode(target).addChild(dragged);
                break;
        }
        updateDroppable();
        updateControls();
    }
    
    /**
     * Remove all listeners upon close.
     */
    private void onClose(Event event) {
        Page page = window.getPage();
        //TODO: page.removeEventListener(layoutListener);
        page.removeAttribute(CWF_PAGE);
        page.setAttribute(ATTR_BRING_TO_FRONT, bringToFront);
        clipboard.removeListener(window);
    }
}
