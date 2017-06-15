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

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementUI;
import org.carewebframework.shell.layout.Layout;
import org.carewebframework.shell.layout.LayoutParser;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.IDisable;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Menuheader;
import org.carewebframework.web.component.Menuitem;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.page.PageUtil;

/**
 * Context menu for designer.
 */
public class DesignContextMenu implements IAutoWired {

    private final Clipboard clipboard = Clipboard.getInstance();

    private BaseComponent listener;

    private ElementUI owner;

    private Menupopup menuPopup;

    @WiredComponent
    private Menuheader mnuHeader;

    @WiredComponent
    private Menuitem mnuAdd;

    @WiredComponent
    private Menuitem mnuDelete;

    @WiredComponent
    private Menuitem mnuCopy;

    @WiredComponent
    private Menuitem mnuPaste;

    @WiredComponent
    private Menuitem mnuCut;

    @WiredComponent
    private Menuitem mnuProperties;

    @WiredComponent
    private Menuitem mnuAbout;

    /**
     * Returns an instance of the design context menu. This is a singleton with the page scope and
     * is cached once created.
     *
     * @return The design context menu for the active page.
     */
    public static DesignContextMenu getInstance() {
        Page page = ExecutionContext.getPage();
        DesignContextMenu contextMenu = page.getAttribute(DesignConstants.ATTR_DESIGN_MENU, DesignContextMenu.class);

        if (contextMenu == null) {
            contextMenu = create();
            page.setAttribute(DesignConstants.ATTR_DESIGN_MENU, contextMenu);
        }

        return contextMenu;
    }

    /**
     * Creates an instance of the design context menu.
     *
     * @return New design context menu.
     */
    public static DesignContextMenu create() {
        return PageUtil.createPage(DesignConstants.RESOURCE_PREFIX + "designContextMenu.cwf", ExecutionContext.getPage())
                .get(0).getAttribute("controller", DesignContextMenu.class);
    }

    /**
     * Updates states on input elements (typically buttons or menu items) according to state of the
     * specified UI element. Any parameter may be null.
     *
     * @param ele A UI element.
     * @param add Add input element.
     * @param delete Delete input element.
     * @param copy Copy input element.
     * @param cut Cut input element.
     * @param paste Paste input element.
     * @param properties Properties input element.
     * @param about About input element.
     */
    public void updateStates(ElementBase ele, IDisable add, IDisable delete, IDisable copy, IDisable cut, IDisable paste,
                             IDisable properties, IDisable about) {
        boolean isNull = ele == null;
        boolean isLocked = isNull || ele.isLocked();
        boolean noDelete = isLocked || ele.getDefinition().isInternal();
        boolean noAdd = isLocked || !ele.canAcceptChild();
        boolean noEdit = isLocked || !ele.getDefinition().hasEditableProperties();
        Object cbData = Clipboard.getInstance().getData();
        Class<? extends ElementBase> clazz = cbData instanceof Layout ? ((Layout) cbData).getRootClass() : null;
        boolean noPaste = noAdd || clazz == null || !ele.canAcceptChild(clazz)
                || !ElementBase.canAcceptParent(clazz, ele.getClass());
        disable(add, noAdd);
        disable(delete, noDelete);
        disable(copy, isNull);
        disable(cut, noDelete);
        disable(paste, noPaste);
        disable(properties, noEdit);
        disable(about, isNull);
    }

    /**
     * Sets the disabled state of the specified component.
     *
     * @param comp The component.
     * @param disabled The disabled state.
     */
    private void disable(IDisable comp, boolean disabled) {
        if (comp != null) {
            comp.setDisabled(disabled);

            if (comp instanceof BaseUIComponent) {
                ((BaseUIComponent) comp).addStyle("opacity", disabled ? ".2" : "1");
            }
        }
    }

    @Override
    public void afterInitialized(BaseComponent root) {
        menuPopup = (Menupopup) root;
        root.setAttribute("controller", this);
        mnuHeader.setImage(DesignConstants.DESIGN_ICON_ACTIVE);
        clipboard.addListener(mnuHeader);
        menuPopup.addEventListener("open", (event) -> {
            onOpen(event);
        });
    }

    /**
     * Update control states based on menu owner.
     */
    private void updateControls() {
        if (owner == null) {
            menuPopup.close();
        } else {
            updateStates(owner, mnuAdd, mnuDelete, mnuCopy, mnuCut, mnuPaste, mnuProperties, mnuAbout);
        }
    }

    /**
     * Sets the context menu's owner. This modifies the member menu item states to reflect the
     * current owner.
     *
     * @param owner Menu's owner.
     */
    public void setOwner(ElementUI owner) {
        if (this.owner != owner) {
            this.owner = owner;
            mnuHeader.setLabel(
                owner == null ? "" : StrUtil.formatMessage("@cwf.shell.designer.menu.title", owner.getDisplayName()));
            updateControls();
        }
    }

    /**
     * Sets the component that is to receive layout change events.
     *
     * @param listener Event listener.
     */
    public void setListener(BaseComponent listener) {
        this.listener = listener;
    }

    public Menupopup getMenupopup() {
        return menuPopup;
    }

    /**
     * Sets the context menu's owner based on the UI element that invoked the menu.
     *
     * @param event The open event.
     */
    private void onOpen(Event event) {
        if (listener == null) {
            BaseComponent ref = event.getRelatedTarget();
            setOwner(ElementUI.getAssociatedElement(ref));
        }

        if (owner == null) {
            event.stopPropagation();
            menuPopup.close();
        }
    }

    /**
     * Invoke owner's property editor.
     */
    @EventHandler(value = "click", target = "@mnuProperties")
    private void onClick$mnuProperties() {
        owner.editProperties();
    }

    /**
     * Invoke owner's about dialog.
     */
    @EventHandler(value = "click", target = "@mnuAbout")
    private void onClick$mnuAbout() {
        owner.about();
    }

    /**
     * Remove owner from the layout.
     */
    @EventHandler(value = "click", target = "@mnuDelete")
    private void onClick$mnuDelete() {
        owner.remove(true);
    }

    /**
     * Present Add Component dialog containing valid choices for this owner.
     */
    @EventHandler(value = "click", target = "@mnuAdd")
    private void onClick$mnuAdd() {
        AddComponent.newChild(owner, null);
    }

    /**
     * Copies the XML layout with the owner as the root node to the clipboard.
     */
    @EventHandler(value = "click", target = "@mnuCopy")
    private void onClick$mnuCopy() {
        clipboard.copy(LayoutParser.parseElement(owner));
    }

    /**
     * Copies the XML layout with the owner as the root node to the clipboard, then deletes the
     * owner.
     */
    @EventHandler(value = "click", target = "@mnuCut")
    private void onClick$mnuCut() {
        onClick$mnuCopy();
        onClick$mnuDelete();
    }

    /**
     * Paste the XML layout in the clipboard into the layout with the owner as the parent node.
     */
    @EventHandler(value = "click", target = "@mnuPaste")
    private void onClick$mnuPaste() {
        Object data = clipboard.getData();

        if (data instanceof Layout) {
            ((Layout) data).materialize(owner);
        }
    }

    /**
     * Display the clipboard viewer.
     */
    @EventHandler(value = "click", target = "mnuView")
    private void onClick$mnuView() {
        clipboard.view();
    }

    /**
     * Called when clipboard contents changes.
     */
    @EventHandler(value = "clipboardChange", target = "@mnuHeader")
    private void onClipboardChange() {
        updateControls();
    }
}
