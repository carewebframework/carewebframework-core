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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementCWFBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.ancillary.IDisable;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.WiredComponentScanner;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Menuitem;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageParser;

/**
 * Context menu for designer.
 */
public class DesignContextMenu extends Menupopup implements INamespace {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(DesignContextMenu.class);
    
    private UIElementBase owner;
    
    private Menuitem mnuHeader;
    
    private Menuitem mnuAdd;
    
    private Menuitem mnuDelete;
    
    private Menuitem mnuCopy;
    
    private Menuitem mnuPaste;
    
    private Menuitem mnuCut;
    
    private Menuitem mnuProperties;
    
    private Menuitem mnuAbout;
    
    private final Clipboard clipboard = Clipboard.getInstance();
    
    private BaseComponent listener;
    
    /**
     * Returns an instance of the design context menu. This is a singleton with the desktop scope
     * and is cached once created.
     * 
     * @return The design context menu for the active destkop.
     */
    public static DesignContextMenu getInstance() {
        Page page = ExecutionContext.getPage();
        DesignContextMenu contextMenu = (DesignContextMenu) page.getAttribute(DesignConstants.ATTR_DESIGN_MENU);
        
        if (contextMenu == null) {
            contextMenu = create();
            page.setAttribute(DesignConstants.ATTR_DESIGN_MENU, contextMenu);
            ZKUtil.suppressContextMenu(contextMenu);
        }
        
        return contextMenu;
    }
    
    /**
     * Creates an instance of the design context menu.
     * 
     * @return New design context menu.
     */
    public static DesignContextMenu create() {
        DesignContextMenu contextMenu = null;
        
        try {
            PageDefinition def = PageParser.getInstance().parse(DesignConstants.RESOURCE_PREFIX + "DesignContextMenu.cwf");
            contextMenu = (DesignContextMenu) def.materialize(null);
            WiredComponentScanner.wire(contextMenu, contextMenu);
            contextMenu.mnuHeader.setImage(DesignConstants.DESIGN_ICON_ACTIVE);
            contextMenu.clipboard.addListener(contextMenu);
        } catch (Exception e) {
            log.error("Error creating design context menu.", e);
            
            if (contextMenu != null) {
                contextMenu.detach();
                contextMenu = null;
            }
        }
        
        return contextMenu;
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
    public static void updateStates(UIElementBase ele, IDisable add, IDisable delete, IDisable copy, IDisable cut,
                                    IDisable paste, IDisable properties, IDisable about) {
        boolean isNull = ele == null;
        boolean isLocked = isNull || ele.isLocked();
        boolean noDelete = isLocked || ele.getDefinition().isInternal();
        boolean noAdd = isLocked || !ele.canAcceptChild();
        boolean noEdit = isLocked || !ele.getDefinition().hasEditableProperties();
        Object cbData = Clipboard.getInstance().getData();
        Class<? extends UIElementBase> clazz = cbData instanceof UILayout ? ((UILayout) cbData).getRootClass() : null;
        boolean noPaste = noAdd || clazz == null || !ele.canAcceptChild(clazz)
                || !UIElementBase.canAcceptParent(clazz, ele.getClass());
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
    private static void disable(IDisable comp, boolean disabled) {
        if (comp != null) {
            comp.setDisabled(disabled);
            
            if (comp instanceof BaseUIComponent) {
                ((BaseUIComponent) comp).addStyle("opacity", disabled ? ".2" : "1");
            }
        }
    }
    
    /**
     * Update control states based on menu owner.
     */
    private void updateControls() {
        if (owner == null) {
            close();
        } else {
            updateStates(owner, mnuAdd, mnuDelete, mnuCopy, mnuCut, mnuPaste, mnuProperties, mnuAbout);
        }
    }
    
    /**
     * Avoid exception if menu not attached to a desktop.
     */
    @Override
    public void close() {
        if (getDesktop() != null) {
            super.close();
        }
    }
    
    /**
     * Sets the context menu's owner. This modifies the member menu item states to reflect the
     * current owner.
     * 
     * @param owner Menu's owner.
     */
    public void setOwner(UIElementBase owner) {
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
    
    /**
     * Sets the context menu's owner based on the UI element that invoked the menu.
     * 
     * @param event The open event.
     */
    public void onOpen(Event event) {
        if (listener == null) {
            BaseComponent ref = ((OpenEvent) event).getReference();
            setOwner(UIElementCWFBase.getAssociatedUIElement(ref));
        }
        
        if (owner == null) {
            event.stopPropagation();
            this.close();
        }
    }
    
    /**
     * Invoke owner's property editor.
     */
    public void onClick$mnuProperties() {
        owner.editProperties();
    }
    
    /**
     * Invoke owner's about dialog.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$mnuAbout() throws Exception {
        owner.about();
    }
    
    /**
     * Remove owner from the layout.
     */
    public void onClick$mnuDelete() {
        owner.remove(true);
    }
    
    /**
     * Present Add Component dialog containing valid choices for this owner.
     */
    public void onClick$mnuAdd() {
        AddComponent.newChild(owner);
    }
    
    /**
     * Copies the XML layout with the owner as the root node to the clipboard.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$mnuCopy() throws Exception {
        clipboard.copy(UILayout.serialize(owner));
    }
    
    /**
     * Copies the XML layout with the owner as the root node to the clipboard, then deletes the
     * owner.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$mnuCut() throws Exception {
        onClick$mnuCopy();
        onClick$mnuDelete();
    }
    
    /**
     * Paste the XML layout in the clipboard into the layout with the owner as the parent node.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$mnuPaste() throws Exception {
        Object data = clipboard.getData();
        
        if (data instanceof UILayout) {
            ((UILayout) data).deserialize(owner);
        }
    }
    
    /**
     * Display the clipboard viewer.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$mnuView() throws Exception {
        clipboard.view();
    }
    
    /**
     * Called when clipboard contents changes.
     */
    public void onClipboardChange() {
        updateControls();
    }
}
