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
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.web.annotation.WiredComponentScanner;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageParser;

/**
 * Allows viewing and editing of clipboard contents.
 */
public class ClipboardViewer extends Window {
    
    private Clipboard clipboard;
    
    private Object data;
    
    private Textbox txtData;
    
    private Button btnSave;
    
    private Button btnRestore;
    
    private boolean modified;
    
    private final String MSG_EMPTY = StrUtil.getLabel("cwf.shell.clipboard.viewer.message.empty");
    
    /**
     * Show viewer.
     * 
     * @param clipboard Clipboard whose contents is to be accessed.
     * @throws Exception Unspecified exception.
     */
    public static void execute(Clipboard clipboard) throws Exception {
        PageDefinition def = PageParser.getInstance().parse(DesignConstants.RESOURCE_PREFIX + "ClipboardViewer.cwf");
        ClipboardViewer viewer = (ClipboardViewer) PopupDialog.popup(def, null, true, true, false);
        viewer.clipboard = clipboard;
        viewer.data = clipboard.getData();
        WiredComponentScanner.wire(viewer, viewer);
        viewer.restore();
        viewer.setMode(Mode.MODAL);
    }
    
    /**
     * Commit changes in viewer to clipboard.
     * 
     * @return True if successful.
     */
    private boolean commit() {
        if (modified) {
            String text = txtData.getValue();
            try {
                clipboard.copy(data instanceof String ? text
                        : data instanceof IClipboardAware ? ((IClipboardAware<?>) data).fromClipboard(text) : null);
            } catch (Exception e) {
                //TODO: Clients.wrongValue(txtData, ZKUtil.formatExceptionForDisplay(e));
                txtData.focus();
                return false;
            }
            modified = false;
            updateControls();
        }
        
        return true;
    }
    
    /**
     * Restore changes from clipboard.
     */
    private void restore() {
        String text = data == null ? MSG_EMPTY
                : data instanceof IClipboardAware ? ((IClipboardAware<?>) data).toClipboard() : data.toString();
        txtData.setValue(text);
        txtData.setReadonly(!(data instanceof String || data instanceof IClipboardAware));
        modified = false;
        updateControls();
    }
    
    /**
     * Update control states.
     */
    private void updateControls() {
        btnSave.setDisabled(!modified);
        btnRestore.setDisabled(!modified);
        //TODO: Clients.clearWrongValue(txtData);
    }
    
    /**
     * Detected data edits.
     */
    public void onChanging$txtData() {
        modified = true;
        updateControls();
    }
    
    /**
     * Clicking OK button commits changes and closes viewer.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnOK() throws Exception {
        if (commit()) {
            detach();
        }
    }
    
    /**
     * Clicking cancel button discards changes and closes viewer.
     */
    public void onClick$btnCancel() {
        detach();
    }
    
    /**
     * Clicking save button commits changes.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnSave() throws Exception {
        commit();
    }
    
    /**
     * Clicking restore button restores original data.
     */
    public void onClick$btnRestore() {
        restore();
    }
}
