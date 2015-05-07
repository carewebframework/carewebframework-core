/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.testharness;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Plugin to facilitate testing of zul layouts.
 */
public class SandboxController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String[] REPLACE_MODES = { "modal", "highlighted", "popup" };
    
    private Textbox txtContent;
    
    private Component contentParent;
    
    private Component contentBase;
    
    /**
     * Find the content base component. We can't assign it an id because of potential id collisions.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        contentBase = ZKUtil.findChild(contentParent, Idspace.class);
    }
    
    /**
     * Refreshes the view based on the current contents.
     */
    @Override
    public void refresh() {
        super.refresh();
        ZKUtil.detachChildren(contentBase);
        String content = txtContent.getText();
        
        if (content != null && !content.isEmpty()) {
            try {
                Events.echoEvent("onModeCheck", this.root, null);
                Executions.createComponentsDirectly(txtContent.getText(), null, contentBase, null);
            } catch (Exception e) {
                ZKUtil.detachChildren(contentBase);
                Label label = new Label(ExceptionUtils.getStackTrace(e));
                label.setMultiline(true);
                contentBase.appendChild(label);
            }
        }
    }
    
    /**
     * Process a focus request.
     */
    public void onFocus() {
        txtContent.setFocus(true);
    }
    
    /**
     * Refocus the text box. This is deferred to prevent closure of any window with a popup mode.
     */
    private void focus() {
        Events.echoEvent(Events.ON_FOCUS, this.root, null);
    }
    
    /**
     * Check for unsupported window modes. This is done asynchronously to allow modal windows to
     * also be checked.
     */
    public void onModeCheck() {
        modeCheck(contentBase);
    }
    
    /**
     * Check for any window components with mode settings that need to be changed.
     * 
     * @param comp Current component in search.
     */
    private void modeCheck(Component comp) {
        if (comp instanceof Window) {
            Window win = (Window) comp;
            
            if (win.isVisible() && ArrayUtils.contains(REPLACE_MODES, win.getMode())) {
                win.setMode("overlapped");
            }
        }
        
        for (Component child : comp.getChildren()) {
            modeCheck(child);
        }
    }
    
    /**
     * Renders the zul content in the view pane.
     */
    public void onClick$btnRenderContent() {
        refresh();
        focus();
    }
    
    /**
     * Clears the zul content.
     */
    public void onClick$btnClearContent() {
        txtContent.setText(null);
        focus();
    }
    
    /**
     * Clears the view pane.
     */
    public void onClick$btnClearView() {
        ZKUtil.detachChildren(contentBase);
        focus();
    }
    
    /**
     * Re-renders content in the view pane.
     */
    public void onClick$btnRefreshView() {
        refresh();
        focus();
    }
    
    /**
     * Set text box focus upon activation.
     */
    @Override
    public void onActivate() {
        super.onActivate();
        focus();
    }
    
}
