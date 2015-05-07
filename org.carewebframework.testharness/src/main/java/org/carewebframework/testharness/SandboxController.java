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

import org.apache.commons.lang.exception.ExceptionUtils;

import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

/**
 * Plugin to facilitate testing of zul layouts.
 */
public class SandboxController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox txtContent;
    
    private Component contentParent;
    
    @Override
    public void refresh() {
        super.refresh();
        Component contentBase = contentParent.getFirstChild();
        ZKUtil.detachChildren(contentBase);
        String content = txtContent.getText();
        
        if (content != null && !content.isEmpty()) {
            try {
                Executions.createComponentsDirectly(txtContent.getText(), null, contentBase, null);
            } catch (Exception e) {
                ZKUtil.detachChildren(contentBase);
                Label label = new Label(ExceptionUtils.getStackTrace(e));
                label.setMultiline(true);
                contentBase.appendChild(label);
            }
        }
    }
    
    public void clear() {
        txtContent.setText(null);
        refresh();
    }
    
    private void focus() {
        txtContent.setFocus(true);
    }
    
    public void onClick$btnView() {
        refresh();
        focus();
    }
    
    public void onClick$btnClear() {
        clear();
        focus();
    }
    
    @Override
    public void onActivate() {
        super.onActivate();
        focus();
    }
    
}
