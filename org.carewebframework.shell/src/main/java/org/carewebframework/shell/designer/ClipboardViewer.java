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

import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.util.ConventionWires;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Allows viewing and editing of clipboard contents.
 */
public class ClipboardViewer extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private Clipboard clipboard;
    
    private Object data;
    
    private Textbox txtData;
    
    private Button btnSave;
    
    private Button btnRestore;
    
    private boolean modified;
    
    private final String MSG_EMPTY = Labels.getLabel("cwf.shell.clipboard.viewer.message.empty");
    
    /**
     * Show viewer.
     * 
     * @param clipboard Clipboard whose contents is to be accessed.
     * @throws Exception
     */
    public static void execute(Clipboard clipboard) throws Exception {
        PageDefinition def = ZKUtil.loadCachedPageDefinition(DesignConstants.RESOURCE_PREFIX + "ClipboardViewer.zul");
        ClipboardViewer viewer = (ClipboardViewer) PopupDialog.popup(def, null, true, true, false);
        viewer.clipboard = clipboard;
        viewer.data = clipboard.getData();
        ConventionWires.wireVariables(viewer, viewer);
        viewer.restore();
        ConventionWires.addForwards(viewer, viewer);
        Events.addEventListeners(viewer, viewer);
        viewer.doModal();
    }
    
    /**
     * Commit changes in viewer to clipboard.
     * 
     * @throws Exception
     */
    private void commit() throws Exception {
        if (modified) {
            String text = txtData.getText();
            clipboard.copy(data instanceof String ? text : data instanceof IClipboardAware ? ((IClipboardAware<?>) data)
                    .fromClipboard(text) : null);
            modified = false;
            updateControls();
        }
    }
    
    /**
     * Restore changes from clipboard.
     */
    private void restore() {
        String text = data == null ? MSG_EMPTY : data instanceof IClipboardAware ? ((IClipboardAware<?>) data).toClipboard()
                : data.toString();
        txtData.setText(text);
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
     * @throws Exception
     */
    public void onClick$btnOK() throws Exception {
        commit();
        detach();
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
     * @throws Exception
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
