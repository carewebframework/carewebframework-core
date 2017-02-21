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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.LayoutIdentifier;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.IResponseCallback;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Radiobutton;
import org.carewebframework.web.component.Radiogroup;
import org.carewebframework.web.component.Upload;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.UploadEvent;
import org.carewebframework.web.event.UploadEvent.UploadState;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ListModel;

/**
 * Supports selection and management of existing layouts.
 */
public class LayoutManager implements IAutoWired {

    private static final String ATTR_DEFAULT_SCOPE = LayoutUtil.class.getName() + ".defaultscope";

    @WiredComponent
    private Button btnOK;

    @WiredComponent
    private Button btnDelete;

    @WiredComponent
    private Button btnRename;

    @WiredComponent
    private Button btnClone;

    @WiredComponent
    private Button btnExport;

    @WiredComponent
    private Button btnImport;

    @WiredComponent
    private Listbox lstLayouts;

    @WiredComponent
    private Label lblPrompt;

    @WiredComponent
    private Radiogroup radioGroup;

    @WiredComponent
    private Radiobutton rbShared;

    @WiredComponent
    private BaseUIComponent tbManage;

    @WiredComponent
    private BaseUIComponent pnlSelect;

    @WiredComponent
    private BaseUIComponent pnlScope;

    @WiredComponent
    private Upload upload;

    private boolean shared;

    private IModelAndView<Listitem, String> modelAndView;

    private final IComponentRenderer<Listitem, String> renderer = new IComponentRenderer<Listitem, String>() {

        @Override
        public Listitem render(String data) {
            Listitem item = new Listitem(data);
            item.setData(new LayoutIdentifier(data, shared));

            if (pnlSelect.isVisible()) {
                item.addEventForward(DblclickEvent.TYPE, btnOK, ClickEvent.TYPE);
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
     * @param closeListener Close event listener.
     */
    public static void show(boolean manage, String deflt, IEventListener closeListener) {
        Map<String, Object> args = new HashMap<>();
        args.put("manage", manage);
        args.put("deflt", deflt);
        PopupDialog.show(RESOURCE_PREFIX + "layoutManager.cwf", args, true, true, true, closeListener);
    }

    /**
     * Prompts to save layout.
     *
     * @param layout Layout to save.
     * @param layoutId Layout identifier
     * @param hideScope If true, hide shared/private scope selection.
     * @param callback Callback if layout successfully saved.
     */
    public static void saveLayout(UILayout layout, LayoutIdentifier layoutId, boolean hideScope,
                                  IResponseCallback<LayoutIdentifier> callback) {
        LayoutPrompt.show(layoutId, hideScope, true, CAP_LAYOUT_SAVE, MSG_LAYOUT_SAVE, (event) -> {
            LayoutIdentifier id = event.getTarget().getAttribute("layoutId", LayoutIdentifier.class);

            if (id != null) {
                layout.saveToProperty(id);

                if (callback != null) {
                    callback.onComplete(id);
                }
            }

        });
    }

    public static void exportLayout(LayoutIdentifier layout) {
        String content = LayoutUtil.getLayoutContent(layout);
        ClientUtil.saveToFile(content, "text/xml", layout.name + ".xml");
    }

    private Window window;

    @Override
    public void afterInitialized(BaseComponent root) {
        window = (Window) root;
        shared = defaultIsShared();
        boolean manage = root.getAttribute("manage", false);
        window.setTitle(StrUtil.formatMessage(manage ? CAP_LAYOUT_MANAGE : CAP_LAYOUT_LOAD));
        lblPrompt.setLabel(StrUtil.formatMessage(manage ? MSG_LAYOUT_MANAGE : MSG_LAYOUT_LOAD));
        modelAndView = lstLayouts.getModelAndView(String.class);
        modelAndView.setRenderer(renderer);
        pnlSelect.setVisible(!manage);
        tbManage.setVisible(manage);
        ((Radiobutton) radioGroup.getChildAt(shared ? 0 : 1)).setChecked(true);
        pnlScope.addClass(manage ? "pull-right" : "pull-left");
        upload.bind(btnImport);
        refresh(root.getAttribute("dflt", ""));
    }

    /**
     * Refresh the list.
     *
     * @param deflt The layout to select initially.
     */
    private void refresh(String deflt) {
        modelAndView.setModel(new ListModel<>(LayoutUtil.getLayouts(shared)));
        lstLayouts.setSelectedItem(deflt == null ? null : (Listitem) lstLayouts.findChildByLabel(deflt));
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
        LayoutPrompt.show(layoutId1, !clone, false, title, prompt, (event) -> {
            LayoutIdentifier layoutId2 = event.getTarget().getAttribute("layoutId", LayoutIdentifier.class);

            if (layoutId2 != null) {
                if (clone) {
                    LayoutUtil.cloneLayout(layoutId1, layoutId2);
                } else {
                    LayoutUtil.renameLayout(layoutId1, layoutId2.name);
                }

                refresh(null);
            }
        });
    }

    /**
     * Import a layout.
     *
     * @param shared If true, import as a shared layout.
     * @param strm An input stream.
     */
    public void importLayout(boolean shared, InputStream strm) {
        UILayout layout = new UILayout();
        layout.loadFromStream(strm);
        LayoutIdentifier layoutId = new LayoutIdentifier(layout.getName(), shared);
        saveLayout(layout, layoutId, false, (response) -> {
            refresh(response.name);
        });
    }

    @EventHandler(value = "upload", target = "@btnImport")
    private void onUpload$btnImport(UploadEvent event) {
        if (event.getState() == UploadState.DONE) {
            importLayout(shared, event.getBlob());
        }
    }

    /**
     * Sets the selected layout and closes the dialog.
     */
    @EventHandler(value = "click", target = "@btnOK")
    private void onClick$btnOK() {
        LayoutIdentifier id = getSelectedLayout();

        if (id != null) {
            window.setAttribute("layoutId", id);
            window.close();
        }
    }

    @EventHandler(value = "click", target = "btnCancel")
    private void onClick$btnCancel() {
        window.close();
    }

    /**
     * Deletes the selected layout.
     */
    @EventHandler(value = "click", target = "@btnDelete")
    private void onClick$btnDelete() {
        DialogUtil.confirm(MSG_LAYOUT_DELETE, (confirm) -> {
            if (confirm) {
                LayoutUtil.deleteLayout(getSelectedLayout());
                refresh(null);
            }
        });
    }

    /**
     * Renames the selected layout.
     */
    @EventHandler(value = "click", target = "@btnRename")
    private void onClick$btnRename() {
        cloneOrRename(false);
    }

    /**
     * Clones the selected layout.
     */
    @EventHandler(value = "click", target = "@btnClone")
    private void onClick$btnClone() {
        cloneOrRename(true);
    }

    /**
     * Export a layout
     */
    @EventHandler(value = "click", target = "@btnExport")
    private void onClick$btnExport() {
        exportLayout(getSelectedLayout());
    }

    /**
     * Update control states when selection changes.
     */
    @EventHandler(value = "change", target = "@lstLayouts")
    private void onChange$lstLayouts() {
        updateControls();
    }

    /**
     * Refresh when shared/private toggled.
     */
    @EventHandler(value = "change", target = "@radioGroup")
    private void onChange$radioGroup() {
        shared = radioGroup.getSelected() == rbShared;
        defaultIsShared(shared);
        refresh(null);
    }

}
