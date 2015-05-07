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
import org.zkoss.zk.ui.event.Event;
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
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        contentBase = ZKUtil.findChild(contentParent, Idspace.class);
    }
    
    @Override
    public void refresh() {
        super.refresh();
        ZKUtil.detachChildren(contentBase);
        String content = txtContent.getText();
        
        if (content != null && !content.isEmpty()) {
            try {
                Events.echoEvent("onModeCheck", this.root, contentBase);
                Executions.createComponentsDirectly(txtContent.getText(), null, contentBase, null);
            } catch (Exception e) {
                ZKUtil.detachChildren(contentBase);
                Label label = new Label(ExceptionUtils.getStackTrace(e));
                label.setMultiline(true);
                contentBase.appendChild(label);
            }
        }
    }
    
    public void onFocus() {
        txtContent.setFocus(true);
    }
    
    private void focus() {
        Events.echoEvent(Events.ON_FOCUS, this.root, null);
    }
    
    public void onModeCheck(Event event) {
        modeCheck((Component) event.getData());
    }
    
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
    
    public void onClick$btnViewContent() {
        refresh();
        focus();
    }
    
    public void onClick$btnClearContent() {
        txtContent.setText(null);
        focus();
    }
    
    public void onClick$btnClearView() {
        ZKUtil.detachChildren(contentBase);
        focus();
    }
    
    public void onClick$btnRefreshView() {
        refresh();
        focus();
    }
    
    @Override
    public void onActivate() {
        super.onActivate();
        focus();
    }
    
}
