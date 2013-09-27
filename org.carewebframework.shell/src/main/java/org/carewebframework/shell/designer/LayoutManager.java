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
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_OVERWRITE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_RENAME;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_SAVE_PRIVATE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_SAVE_SHARED;
import static org.carewebframework.shell.designer.DesignConstants.ERR_LAYOUT_IMPORT;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_BADNAME;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_CLONE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_DELETE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_DUP;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_IMPORT;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_LOAD;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_MANAGE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_OVERWRITE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_RENAME;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_SAVE;
import static org.carewebframework.shell.designer.DesignConstants.RESOURCE_PREFIX;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;
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
    
    public static class LayoutSelection {
        
        public final boolean shared;
        
        public final String name;
        
        private LayoutSelection(String name, boolean shared) {
            this.name = name;
            this.shared = shared;
        }
    }
    
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
    
    private String selectedLayout;
    
    private final ListitemRenderer<String> renderer = new ListitemRenderer<String>() {
        
        @Override
        public void render(Listitem item, String data, int index) throws Exception {
            item.setLabel(data);
            
            if (btnOK.isVisible()) {
                item.addForward(Events.ON_DOUBLE_CLICK, btnOK, Events.ON_CLICK);
            }
        }
        
    };
    
    /**
     * Invokes the layout manager dialog.
     * 
     * @param manage If true, open in management mode; otherwise, in selection mode.
     * @param shared If true, manage shared layouts; otherwise, manage personal layouts.
     * @param deflt Default layout name.
     * @return
     */
    public static LayoutSelection execute(boolean manage, boolean shared, String deflt) {
        LayoutManager dlg = null;
        
        try {
            dlg = (LayoutManager) PopupDialog.popup(ZKUtil.loadCachedPageDefinition(RESOURCE_PREFIX + "LayoutManager.zul"),
                null, true, true, false);
            return dlg.show(manage, shared, deflt);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Prompts for a layout name.
     * 
     * @param dflt Default name.
     * @param shared Shared or personal layout.
     * @param allowDups If true, duplicate names are allowed.
     * @param title Prompt dialog title.
     * @param prompt Prompt dialog text.
     * @return Layout name, or null if prompt was cancelled.
     */
    public static String promptLayoutName(String dflt, boolean shared, boolean allowDups, String title, String prompt) {
        while (true) {
            
            String layoutName = PromptDialog.input(prompt, title, dflt);
            
            if (StringUtils.isEmpty(layoutName)) {
                return null;
            }
            
            dflt = layoutName;
            
            if (!LayoutUtil.validateName(layoutName)) {
                PromptDialog.showError(MSG_LAYOUT_BADNAME);
                continue;
            }
            
            boolean exists = LayoutUtil.layoutExists(layoutName, shared);
            
            if (!exists) {
                return layoutName;
            }
            
            if (allowDups) {
                if (PromptDialog.confirm(MSG_LAYOUT_OVERWRITE, CAP_LAYOUT_OVERWRITE)) {
                    return layoutName;
                }
                continue;
            }
            
            PromptDialog.showError(MSG_LAYOUT_DUP);
        }
    }
    
    /**
     * Prompts to save layout.
     * 
     * @param layout Layout to save.
     * @param dflt A default name.
     * @param shared Shared or private.
     * @return The saved layout name.
     */
    public static String saveLayout(UILayout layout, String dflt, boolean shared) {
        String layoutName = LayoutManager.promptLayoutName(dflt, shared, true, shared ? CAP_LAYOUT_SAVE_SHARED
                : CAP_LAYOUT_SAVE_PRIVATE, MSG_LAYOUT_SAVE);
        
        if (layoutName != null) {
            layout.saveToProperty(layoutName, shared);
        }
        
        return layoutName;
    }
    
    /**
     * Initialize and display the dialog.
     * 
     * @param manage If true, open in management mode; otherwise, in selection mode.
     * @param shared If true, manage shared layouts; otherwise, manage personal layouts.
     * @param deflt Default layout name.
     * @return The selected layout name (if in selection mode).
     */
    private LayoutSelection show(boolean manage, boolean shared, String deflt) {
        this.shared = shared;
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
        return manage || selectedLayout == null ? null : new LayoutSelection(selectedLayout, this.shared);
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
     * Returns the name of the currently selected layout, or null if none selected.
     * 
     * @return The currently selected layout.
     */
    private String getSelectedLayout() {
        Listitem item = lstLayouts.getSelectedItem();
        return item == null ? null : item.getLabel();
    }
    
    /**
     * Clone or rename a layout.
     * 
     * @param name The layout name.
     * @param clone If true, perform a clone operation; if false, a rename operation.
     */
    private void cloneOrRename(String name, boolean clone) {
        String title = clone ? CAP_LAYOUT_CLONE : CAP_LAYOUT_RENAME;
        String prompt = clone ? MSG_LAYOUT_CLONE : MSG_LAYOUT_RENAME;
        String newName = promptLayoutName(name, shared, false, title, prompt);
        
        if (newName != null) {
            if (clone) {
                LayoutUtil.cloneLayout(name, newName, shared);
            } else {
                LayoutUtil.renameLayout(name, newName, shared);
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
            LayoutUtil.deleteLayout(getSelectedLayout(), shared);
            refresh(null);
        }
    }
    
    /**
     * Renames the selected layout.
     */
    public void onClick$btnRename() {
        cloneOrRename(getSelectedLayout(), false);
    }
    
    /**
     * Clones the selected layout.
     */
    public void onClick$btnClone() {
        cloneOrRename(getSelectedLayout(), true);
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
                String layoutName = saveLayout(layout, layout.getName(), shared);
                
                if (layoutName != null) {
                    refresh(layoutName);
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
        String layout = getSelectedLayout();
        String content = LayoutUtil.getLayout(layout, shared);
        Filedownload.save(content, "text/xml", layout + ".xml");
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
        refresh(null);
    }
    
}
