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

import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_CLONE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_LOAD;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_MANAGE;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_RENAME;
import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_SAVE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_CLONE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_DELETE;
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
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Radiobutton;
import org.carewebframework.web.component.Radiogroup;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.model.ModelAndView;
import org.carewebframework.web.page.PageUtil;

/**
 * Supports selection and management of existing layouts.
 */
public class LayoutManager extends Window {
    
    private static final String ATTR_DEFAULT_SCOPE = LayoutUtil.class.getName() + ".default_scope";
    
    private Button btnOK;
    
    private Button btnDelete;
    
    private Button btnRename;
    
    private Button btnClone;
    
    private Button btnExport;
    
    private Listbox lstLayouts;
    
    private Label lblPrompt;
    
    private Radiogroup radioGroup;
    
    private BaseUIComponent pnlManage;
    
    private BaseUIComponent pnlSelect;
    
    private BaseUIComponent pnlScope;
    
    private boolean shared;
    
    private LayoutIdentifier selectedLayout;
    
    private ModelAndView<Listitem, String> modelAndView;
    
    private final IComponentRenderer<Listitem, String> renderer = new IComponentRenderer<Listitem, String>() {
        
        @Override
        public Listitem render(String data) {
            Listitem item = new Listitem(data);
            item.setData(new LayoutIdentifier(data, shared));
            
            if (pnlSelect.isVisible()) {
                item.registerEventForward(DblclickEvent.TYPE, btnOK, ClickEvent.TYPE);
            }
            
            return item;
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
            dlg = (LayoutManager) PopupDialog.popup(PageUtil.getPageDefinition(RESOURCE_PREFIX + "LayoutManager.cwf"), null,
                true, true, false);
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
     * Import a layout.
     * 
     * @param shared If true, import as a shared layout.
     * @return The layout identifier if the import was successful, null otherwise.
     */
    public static LayoutIdentifier importLayout(boolean shared) {
        /*TODO:
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
                return layoutId;
            } catch (Exception e) {
                PromptDialog.showError(e);
            }
        }
        */
        return null;
    }
    
    public static void exportLayout(LayoutIdentifier layout) {
        String content = LayoutUtil.getLayoutContent(layout);
        ClientUtil.saveToFile(content, "text/xml", layout.name + ".xml");
    }
    
    /**
     * Initialize and display the dialog.
     * 
     * @param manage If true, open in management mode; otherwise, in selection mode.
     * @param deflt Default layout name.
     * @return The selected layout name (if in selection mode).
     */
    private LayoutIdentifier show(boolean manage, String deflt) {
        shared = defaultIsShared();
        wireController(this);
        setTitle(StrUtil.formatMessage(manage ? CAP_LAYOUT_MANAGE : CAP_LAYOUT_LOAD));
        lblPrompt.setLabel(StrUtil.formatMessage(manage ? MSG_LAYOUT_MANAGE : MSG_LAYOUT_LOAD));
        modelAndView = new ModelAndView<>(lstLayouts, null, renderer);
        pnlSelect.setVisible(!manage);
        pnlManage.setVisible(manage);
        ((Radiobutton) radioGroup.getChildAt(shared ? 0 : 1)).setChecked(true);
        pnlScope.addClass(manage ? "pull-right" : "pull-left");
        refresh(deflt);
        modal(null);
        return manage || selectedLayout == null ? null : selectedLayout;
    }
    
    /**
     * Refresh the list.
     * 
     * @param deflt The layout to select initially.
     */
    private void refresh(String deflt) {
        modelAndView.setModel(new ListModel<>(LayoutUtil.getLayouts(shared)));
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
        return item == null ? null : (LayoutIdentifier) item.getData();
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
            close();
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
        LayoutIdentifier layoutId = importLayout(shared);
        
        if (layoutId != null) {
            refresh(layoutId.name);
        }
    }
    
    /**
     * Export a layout
     */
    public void onClick$btnExport() {
        exportLayout(getSelectedLayout());
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
        shared = radioGroup.getSelected().indexOf() == 0;
        defaultIsShared(shared);
        refresh(null);
    }
    
}
