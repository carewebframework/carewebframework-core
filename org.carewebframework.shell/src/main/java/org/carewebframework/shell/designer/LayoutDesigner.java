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

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.elements.UIElementBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.shell.plugins.PluginDefinition;
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
import org.carewebframework.web.event.IEventListener;

/**
 * Controller for dialog for managing the current layout.
 */
public class LayoutDesigner implements IAutoWired {
    
    private static final String DIALOG = DesignConstants.RESOURCE_PREFIX + "layoutDesigner.cwf";
    
    private static final String ATTR_BRING_TO_FRONT = DIALOG + ".BTF";
    
    private final String noDescriptionHint = StrUtil.getLabel("cwf.shell.designer.add.component.description.missing.hint");
    
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
     * Listens for changes to the UI, filtering out all but those associated with a UI element.
     */
    private final IEventListener layoutListener = (event) -> {
        if (UIElementBase.getAssociatedUIElement(event.getRelatedTarget()) != null) {
            requestRefresh();
        }
    };
    
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
        Window dlg = page.getAttribute(DIALOG, Window.class);
        
        if (autoCreate && dlg == null) {
            dlg = DialogUtil.popup(DIALOG, true, true, false);
            page.setAttribute(DIALOG, dlg);
        }
        
        return dlg;
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        window = (Window) comp;
        window.setCloseAction(CloseAction.HIDE);
        comp.setAttribute("controller", this);
        bringToFront = comp.getPage().getAttribute(ATTR_BRING_TO_FRONT, true);
        layoutChangedEvent = new LayoutChangedEvent(comp, null);
        contextMenu.setListener(comp);
        clipboard.addListener(comp);
        comp.getPage().addEventListener("register unregister", layoutListener);
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
        comp.invoke("widget$.effect", "pulsate", Collections.singletonMap("times", 1));
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
     * @param parentNode Tree node that will be the parent node of the subtree.
     * @param selectedElement The currently selected element.
     */
    private void buildTree(UIElementBase root, Treenode parentNode, UIElementBase selectedElement) {
        Treenode node = createNode(root);
        node.setParent(parentNode == null ? tree : parentNode);
        
        if (root == selectedElement) {
            tree.setSelectedNode(node);
        }
        
        for (UIElementBase child : root.getChildren()) {
            buildTree(child, node, selectedElement);
        }
    }
    
    /**
     * Creates a tree node associated with the specified UI element.
     * 
     * @param ele UI element
     * @return Newly created tree node.
     */
    private Treenode createNode(UIElementBase ele) {
        String label = ele.getDisplayName();
        String instanceName = ele.getInstanceName();
        PluginDefinition def = ele.getDefinition();
        
        if (!label.equalsIgnoreCase(instanceName)) {
            label += " - " + instanceName;
        }
        
        Treenode node = new Treenode();
        node.setLabel(label);
        node.setData(ele);
        node.setHint(StringUtils.defaultString(def.getDescription(), noDescriptionHint));
        node.addEventForward(DropEvent.class, window, null);
        node.addEventForward(DblclickEvent.class, btnProperties, ClickEvent.TYPE);
        
        if (!ele.isLocked() && !def.isInternal()) {
            node.setDragid("d" + dragId++);
        }
        
        return node;
    }
    
    /**
     * Returns the tree node containing the component.
     * 
     * @param comp The component whose containing tree node is sought.
     * @return The tree node.
     */
    private Treenode getTreenode(BaseComponent comp) {
        return (Treenode) (comp instanceof Treenode ? comp : comp.getAncestor(Treenode.class));
    }
    
    /**
     * Returns currently selected UI element, or null if none selected.
     * 
     * @return Currently selected element (may be null).
     */
    private UIElementBase selectedElement() {
        return getElement(tree.getSelectedNode());
    }
    
    /**
     * Returns the UI element associated with the given tree node.
     * 
     * @param node A tree node.
     * @return The UI element associated with the tree node.
     */
    private UIElementBase getElement(Treenode node) {
        return (UIElementBase) (node == null ? rootElement : node.getData());
    }
    
    /**
     * Update control states for current selection.
     */
    private void updateControls() {
        Treenode selectedNode = tree.getSelectedNode();
        UIElementBase selectedElement = getElement(selectedNode);
        contextMenu.updateStates(selectedElement, btnAdd, btnDelete, btnCopy, btnCut, btnPaste, btnProperties, btnAbout);
        BaseComponent parent = selectedNode == null ? null : selectedNode.getParent();
        //parent = parent == null ? null : parent.getParent();
        Treenode target = parent instanceof Treenode ? (Treenode) parent : null;
        btnLeft.setDisabled(movementType(selectedNode, target, false) == MovementType.INVALID);
        target = selectedNode == null ? null : (Treenode) selectedNode.getPreviousSibling();
        btnRight.setDisabled(movementType(selectedNode, target, false) == MovementType.INVALID);
        btnUp.setDisabled(movementType(selectedNode, target, true) == MovementType.INVALID);
        target = selectedNode == null ? null : (Treenode) selectedNode.getNextSibling();
        btnDown.setDisabled(movementType(selectedNode, target, true) == MovementType.INVALID);
        btnToFront.addStyle("opacity", bringToFront ? null : "0.5");
        
        if (selectedElement != null) {
            window.setContext(contextMenu.getMenupopup());
            contextMenu.setOwner(selectedElement);
        }
        
        if (selectedNode != null) {
            selectedNode.setSelected(false);
            selectedNode.setSelected(true);
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
    
    private boolean canMove(Treenode node) {
        return node != null && node.getDragid() != null;
    }
    
    /**
     * Updates the drop ids for all tree nodes.
     */
    private void updateDroppable() {
        Iterable<Treenode> nodes = tree.getChildren(Treenode.class);
        
        for (Treenode node : nodes) {
            updateDroppable(node, nodes);
        }
    }
    
    /**
     * Update the drop id for the specified target.
     * 
     * @param target Target tree node.
     * @param nodes Node list.
     */
    private void updateDroppable(Treenode target, Iterable<Treenode> nodes) {
        StringBuilder sb = new StringBuilder();
        
        if (canMove(target)) {
            for (Treenode dragged : nodes) {
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
    @EventHandler(value = "layoutChanged", target = "^")
    private void onLayoutChanged() {
        refresh();
    }
    
    /**
     * Updates tool bar controls when selected changes.
     */
    @EventHandler(value = "change", target = "@tree")
    private void onChange$tree() {
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
     * Performs a cut operation on the selected node.
     */
    @EventHandler(value = "click", target = "@btnCut")
    private void onClick$btnCut() {
        onClick$btnCopy();
        onClick$btnDelete();
    }
    
    /**
     * Performs a copy operation on the selected node.
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
     * Displays the property grid for the currently selected node.
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
        AddComponent.newChild(selectedElement(), (result) -> {
            if (result != null) {
                requestRefresh();
            }
        });
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
        Treenode node = tree.getSelectedNode();
        doDrop(node, (Treenode) node.getPreviousSibling(), true);
    }
    
    @EventHandler(value = "click", target = "@btnDown")
    private void onClick$btnDown() {
        Treenode node = tree.getSelectedNode();
        doDrop((Treenode) node.getNextSibling(), node, true);
    }
    
    @EventHandler(value = "click", target = "@btnRight")
    private void onClick$btnRight() {
        Treenode node = tree.getSelectedNode();
        doDrop(node, (Treenode) node.getPreviousSibling(), false);
    }
    
    @EventHandler(value = "click", target = "@btnLeft")
    private void onClick$btnLeft() {
        Treenode node = tree.getSelectedNode();
        doDrop(node, (Treenode) node.getParent().getParent(), false);
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
    @EventHandler(value = Clipboard.CLIPBOARD_CHANGE_EVENT)
    private void onClipboardChange() {
        updateControls();
    }
    
    /**
     * Handles drop events.
     * 
     * @param event The drop event.
     */
    @EventHandler(value = "drop", target = "^")
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
    @EventHandler("close")
    private void onClose(Event event) {
        Page page = window.getPage();
        page.removeEventListener("register unregister", layoutListener);
        page.removeAttribute(DIALOG);
        page.setAttribute(ATTR_BRING_TO_FRONT, bringToFront);
        clipboard.removeListener(window);
    }
}
