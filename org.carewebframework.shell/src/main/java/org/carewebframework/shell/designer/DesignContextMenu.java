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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementZKBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.ext.Disable;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

/**
 * Context menu for designer.
 */
public class DesignContextMenu extends Menupopup implements IdSpace {
    
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
    
    private Component listener;
    
    /**
     * Returns an instance of the design context menu. This is a singleton with the desktop scope
     * and is cached once created.
     * 
     * @return The design context menu for the active destkop.
     */
    public static DesignContextMenu getInstance() {
        Desktop desktop = Executions.getCurrent().getDesktop();
        DesignContextMenu contextMenu = (DesignContextMenu) desktop.getAttribute(DesignConstants.ATTR_DESIGN_MENU);
        
        if (contextMenu == null) {
            contextMenu = create();
            desktop.setAttribute(DesignConstants.ATTR_DESIGN_MENU, contextMenu);
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
            PageDefinition def = ZKUtil.loadCachedPageDefinition(DesignConstants.RESOURCE_PREFIX + "DesignContextMenu.zul");
            contextMenu = (DesignContextMenu) Executions.createComponents(def, null, null);
            ZKUtil.wireController(contextMenu);
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
    public static void updateStates(UIElementBase ele, Disable add, Disable delete, Disable copy, Disable cut,
                                    Disable paste, Disable properties, Disable about) {
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
    private static void disable(Disable comp, boolean disabled) {
        if (comp != null) {
            comp.setDisabled(disabled);
            
            if (comp instanceof HtmlBasedComponent) {
                ((HtmlBasedComponent) comp).setStyle("opacity:" + (disabled ? ".2" : "1"));
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
     * Sets the context menu's owner. This modifies the member menu item states to reflect the
     * current owner.
     * 
     * @param owner Menu's owner.
     */
    public void setOwner(UIElementBase owner) {
        if (this.owner != owner) {
            this.owner = owner;
            mnuHeader.setLabel(owner == null ? "" : StrUtil.formatMessage("@cwf.shell.designer.menu.title",
                owner.getDisplayName()));
            updateControls();
        }
    }
    
    /**
     * Sets the component that is to receive layout change events.
     * 
     * @param listener
     */
    public void setListener(Component listener) {
        this.listener = listener;
    }
    
    /**
     * Sets the context menu's owner based on the UI element that invoked the menu.
     * 
     * @param event
     */
    public void onOpen(Event event) {
        if (listener == null) {
            Component ref = ((OpenEvent) event).getReference();
            setOwner(UIElementZKBase.getAssociatedUIElement(ref));
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
     * @throws Exception
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
     * @throws Exception
     */
    public void onClick$mnuCopy() throws Exception {
        clipboard.copy(UILayout.serialize(owner));
    }
    
    /**
     * Copies the XML layout with the owner as the root node to the clipboard, then deletes the
     * owner.
     * 
     * @throws Exception
     */
    public void onClick$mnuCut() throws Exception {
        onClick$mnuCopy();
        onClick$mnuDelete();
    }
    
    /**
     * Paste the XML layout in the clipboard into the layout with the owner as the parent node.
     * 
     * @throws Exception
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
     * @throws Exception
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
