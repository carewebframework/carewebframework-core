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

import static org.carewebframework.shell.designer.DesignConstants.CAP_LAYOUT_OVERWRITE;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_BADNAME;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_DUP;
import static org.carewebframework.shell.designer.DesignConstants.MSG_LAYOUT_OVERWRITE;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.LayoutIdentifier;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Supports selection and management of existing layouts.
 */
public class LayoutPrompt extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = DesignConstants.RESOURCE_PREFIX + "LayoutPrompt.zul";
    
    private Radiogroup radioGroup;
    
    private Label lblPrompt;
    
    private Textbox txtLayout;
    
    private boolean allowDups;
    
    private LayoutIdentifier layoutId;
    
    /**
     * Prompts for a layout.
     * 
     * @param dflt Default layout.
     * @param hideScope If true, hide private/shared scope selection.
     * @param allowDups If true, duplicate names are allowed.
     * @param title Prompt dialog title.
     * @param prompt Prompt dialog text.
     * @return Layout name, or null if prompt was cancelled.
     */
    public static LayoutIdentifier promptLayout(LayoutIdentifier dflt, boolean hideScope, boolean allowDups, String title,
                                                String prompt) {
        try {
            LayoutPrompt lp = (LayoutPrompt) ZKUtil.loadZulPage(DIALOG, null);
            ZKUtil.wireController(lp);
            lp.setTitle(StrUtil.formatMessage(title));
            lp.lblPrompt.setValue(StrUtil.formatMessage(prompt));
            lp.txtLayout.setText(dflt == null ? null : dflt.name);
            boolean shared = dflt == null ? LayoutManager.defaultIsShared() : dflt.shared;
            lp.radioGroup.setSelectedIndex(shared ? 0 : 1);
            lp.radioGroup.setVisible(!hideScope);
            lp.allowDups = allowDups;
            lp.doModal();
            return lp.layoutId;
        } catch (Exception e) {
            return null;
        }
    }
    
    public void onChanging$txtLayout() {
        showError(null);
    }
    
    public void onClick$btnOK() {
        String name = txtLayout.getText().trim();
        
        if (!LayoutUtil.validateName(name)) {
            showError(MSG_LAYOUT_BADNAME);
            return;
        }
        
        LayoutIdentifier id = new LayoutIdentifier(name, radioGroup.getSelectedIndex() == 0);
        
        if (LayoutUtil.layoutExists(id)) {
            if (!allowDups) {
                showError(MSG_LAYOUT_DUP);
                return;
            } else if (!PromptDialog.confirm(MSG_LAYOUT_OVERWRITE, CAP_LAYOUT_OVERWRITE)) {
                return;
            }
        }
        
        layoutId = id;
        LayoutManager.defaultIsShared(id.shared);
        detach();
    }
    
    private void showError(String message) {
        if (message == null) {
            Clients.clearWrongValue(txtLayout);
        } else {
            Clients.wrongValue(txtLayout, StrUtil.formatMessage(message));
        }
    }
}
