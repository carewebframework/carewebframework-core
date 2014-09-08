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

import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_CLONE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_IMPORT;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_LOAD;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_MANAGE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_RENAME;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_SAVE;
import static org.carewebframework.shell.designer.DesignConstants.ERR_LAYOUT_IMPORT;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_CLONE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_DELETE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_IMPORT;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_LOAD;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_MANAGE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_RENAME;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_SAVE;
import static org.carewebframework.shell.designer.DesignConstants.RESOURCE_PREFIX;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.LayoutIdentifier;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Window;

/**
 * Supports selection and management of existing layouts.
 */
public class LayoutManager extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ATTR_DEFAULT_SCOPE = LayoutUtil.class.getName() + ".default_scope";
    
    private Button btnOK;
    
    private Button btnCancel;
    
    private Button btnDelete;
    
    private Button btnRename;
    
    private Button btnClone;
    
    private Button btnExport;
    
    private Listbox lstLayouts;
    
    private Label lblPrompt;
    
    private Radiogroup radioGroup;
    
    private Toolbar tbarManage;
    
    private boolean shared;
    
    private LayoutIdentifier selectedLayout;
    
    private final ListitemRenderer<String> renderer = new ListitemRenderer<String>() {
        
        @Override
        public void render(Listitem item, String data, int index) throws Exception {
            item.setLabel(data);
            item.setValue(new LayoutIdentifier(data, shared));
            
            if (btnOK.isVisible()) {
                item.addForward(Events.ON_DOUBLE_CLICK, btnOK, Events.ON_CLICK);
            }
        }
        
    };
    
    /**
     * Returns true if the default layout scope is shared.
     * 
     * @return True if the default layout scope is shared.
     */
    public static boolean defaultIsShared() {
        return FrameworkUtil.getAttribute(ATTR_DEFAULT_SCOPE) != null;
    }
    
    /**
     * Sets the default layout scope.
     * 
     * @param isShared If true, the default scope is shared. If false, it is private.
     */
    public static void defaultIsShared(boolean isShared) {
        FrameworkUtil.setAttribute(ATTR_DEFAULT_SCOPE, isShared ? true : null);
    }
    
    /**
     * Invokes the layout manager dialog.
     * 
     * @param manage If true, open in management mode; otherwise, in selection mode.
     * @param deflt Default layout name.
     * @return The layout selected on dialog closure.
     */
    public static LayoutIdentifier execute(boolean manage, String deflt) {
        LayoutManager dlg = null;
        
        try {
            dlg = (LayoutManager) PopupDialog.popup(ZKUtil.loadCachedPageDefinition(RESOURCE_PREFIX + "LayoutManager.zul"),
                null, true, true, false);
            return dlg.show(manage, deflt);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Prompts to save layout.
     * 
     * @param layout Layout to save.
     * @param layoutId Layout identifier
     * @param hideScope If true, hide shared/private scope selection.
     * @return The saved layout name.
     */
    public static LayoutIdentifier saveLayout(UILayout layout, LayoutIdentifier layoutId, boolean hideScope) {
        layoutId = LayoutPrompt.promptLayout(layoutId, hideScope, true, CAP_LAYOUT_SAVE, MSG_LAYOUT_SAVE);
        
        if (layoutId != null) {
            layout.saveToProperty(layoutId);
        }
        
        return layoutId;
    }
    
    /**
     * Initialize and display the dialog.
     * 
     * @param manage If true, open in management mode; otherwise, in selection mode.
     * @param deflt Default layout name.
     * @return The selected layout name (if in selection mode).
     */
    private LayoutIdentifier show(boolean manage, String deflt) {
        this.shared = defaultIsShared();
        ZKUtil.wireController(this);
        setTitle(StrUtil.formatMessage(manage ? CAP_LAYOUT_MANAGE : CAP_LAYOUT_LOAD));
        lblPrompt.setValue(StrUtil.formatMessage(manage ? MSG_LAYOUT_MANAGE : MSG_LAYOUT_LOAD));
        lstLayouts.setItemRenderer(renderer);
        btnOK.setVisible(!manage);
        btnCancel.setVisible(!manage);
        tbarManage.setVisible(manage);
        radioGroup.setSelectedIndex(shared ? 0 : 1);
        refresh(deflt);
        doModal();
        return manage || selectedLayout == null ? null : selectedLayout;
    }
    
    /**
     * Refresh the list.
     * 
     * @param deflt The layout to select initially.
     */
    private void refresh(String deflt) {
        lstLayouts.setModel(new ListModelList<String>(LayoutUtil.getLayouts(shared)));
        lstLayouts.setSelectedIndex(deflt == null ? -1 : ListUtil.findListboxItem(lstLayouts, deflt));
        updateControls();
    }
    
    /**
     * Update control states.
     */
    private void updateControls() {
        boolean disable = getSelectedLayout() == null;
        btnDelete.setDisabled(disable);
        btnOK.setDisabled(disable);
        btnRename.setDisabled(disable);
        btnClone.setDisabled(disable);
        btnExport.setDisabled(disable);
    }
    
    /**
     * Returns the identifier of the currently selected layout, or null if none selected.
     * 
     * @return The currently selected layout.
     */
    private LayoutIdentifier getSelectedLayout() {
        Listitem item = lstLayouts.getSelectedItem();
        return item == null ? null : (LayoutIdentifier) item.getValue();
    }
    
    /**
     * Clone or rename a layout.
     * 
     * @param clone If true, perform a clone operation; if false, a rename operation.
     */
    private void cloneOrRename(boolean clone) {
        String title = clone ? CAP_LAYOUT_CLONE : CAP_LAYOUT_RENAME;
        String prompt = clone ? MSG_LAYOUT_CLONE : MSG_LAYOUT_RENAME;
        LayoutIdentifier layoutId1 = getSelectedLayout();
        LayoutIdentifier layoutId2 = LayoutPrompt.promptLayout(layoutId1, !clone, false, title, prompt);
        
        if (layoutId2 != null) {
            if (clone) {
                LayoutUtil.cloneLayout(layoutId1, layoutId2);
            } else {
                LayoutUtil.renameLayout(layoutId1, layoutId2.name);
            }
            
            refresh(null);
        }
    }
    
    /**
     * Sets the selected layout and closes the dialog.
     */
    public void onClick$btnOK() {
        selectedLayout = getSelectedLayout();
        
        if (selectedLayout != null) {
            onClose();
        }
    }
    
    /**
     * Deletes the selected layout.
     */
    public void onClick$btnDelete() {
        if (PromptDialog.confirm(MSG_LAYOUT_DELETE)) {
            LayoutUtil.deleteLayout(getSelectedLayout());
            refresh(null);
        }
    }
    
    /**
     * Renames the selected layout.
     */
    public void onClick$btnRename() {
        cloneOrRename(false);
    }
    
    /**
     * Clones the selected layout.
     */
    public void onClick$btnClone() {
        cloneOrRename(true);
    }
    
    /**
     * Import a layout.
     */
    public void onClick$btnImport() {
        while (true) {
            try {
                Media media = Fileupload.get(StrUtil.formatMessage(MSG_LAYOUT_IMPORT),
                    StrUtil.formatMessage(CAP_LAYOUT_IMPORT), false);
                
                if (media == null) {
                    break;
                }
                
                if (!"text/xml".equalsIgnoreCase(media.getContentType())) {
                    PromptDialog.showError(ERR_LAYOUT_IMPORT);
                    continue;
                }
                
                UILayout layout = new UILayout();
                layout.loadFromText(media.getStringData());
                LayoutIdentifier layoutId = saveLayout(layout, new LayoutIdentifier(layout.getName(), shared), false);
                
                if (layoutId != null) {
                    refresh(layoutId.name);
                }
                
                break;
            } catch (Exception e) {
                PromptDialog.showError(e);
            }
        }
    }
    
    /**
     * Export a layout
     */
    public void onClick$btnExport() {
        LayoutIdentifier layout = getSelectedLayout();
        String content = LayoutUtil.getLayoutContent(layout);
        Filedownload.save(content, "text/xml", layout.name + ".xml");
    }
    
    /**
     * Update control states when selection changes.
     */
    public void onSelect$lstLayouts() {
        updateControls();
    }
    
    /**
     * Refresh when shared/private toggled.
     */
    public void onCheck$radioGroup() {
        shared = radioGroup.getSelectedIndex() == 0;
        defaultIsShared(shared);
        refresh(null);
    }
    
}
