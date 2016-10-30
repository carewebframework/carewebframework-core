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
package org.carewebframework.security.spring.controller;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.EventUtil;

/**
 * Controller for the password change dialog.
 */
public class PasswordChangeController implements IAutoWired {
    
    private static final long serialVersionUID = 1L;
    
    private BaseUIComponent panel;
    
    private Textbox txtUsername;
    
    private Textbox txtPassword;
    
    private Textbox txtPassword1;
    
    private Textbox txtPassword2;
    
    private Label lblTitle;
    
    private Label lblInfo;
    
    private Label lblMessage;
    
    private IUser user;
    
    private boolean forced;
    
    private ISecurityService securityService;
    
    private final String MESSAGE_PASSWORD_RULES = StrUtil.getLabel("password.change.rules.label");
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        panel = (BaseUIComponent) comp;
        forced = !FrameworkUtil.isInitialized();
        String title;
        String label;
        
        if (!forced) {
            user = UserContext.getActiveUser();
            title = "password.change.dialog.panel.title";
            label = "password.change.dialog.label";
        } else {
            user = (IUser) comp.getAttribute("user");
            title = "password.change.dialog.expired.panel.title";
            label = "password.change.dialog.expired.label";
        }
        
        if (user == null) {
            doCancel();
        } else {
            lblTitle.setLabel(StrUtil.getLabel(title) + " - " + user.getFullName());
            lblInfo.setLabel(StrUtil.getLabel(label, MESSAGE_PASSWORD_RULES));
        }
    }
    
    /**
     * Pressing return in the current password text box moves to the new password text box.
     */
    public void onOK$txtPassword() {
        txtPassword1.setFocus(true);
        txtPassword1.selectAll();
    }
    
    /**
     * Pressing return in the new password text box moves to the confirm password text box.
     */
    public void onOK$txtPassword1() {
        txtPassword2.setFocus(true);
        txtPassword2.selectAll();
    }
    
    /**
     * Pressing return in confirm password text box submits the form.
     */
    public void onOK$txtPassword2() {
        doSubmit();
    }
    
    /**
     * Submits the form when OK button is clicked.
     */
    public void onClick$btnOK() {
        doSubmit();
    }
    
    /**
     * Cancels the form when the Cancel button is clicked.
     */
    public void onClick$btnCancel() {
        doCancel();
    }
    
    /**
     * Cancel the password change request.
     */
    private void doCancel() {
        if (!forced) {
            panel.getPage().detach();
        } else {
            EventUtil.send("close", panel.getPage(), StrUtil.getLabel("password.change.dialog.password.change.canceled"));
        }
    }
    
    /**
     * Submits the authentication request.
     */
    private void doSubmit() {
        showMessage("");
        String password = txtPassword.getValue().trim();
        String password1 = txtPassword1.getValue().trim();
        String password2 = txtPassword2.getValue().trim();
        
        if (!securityService.validatePassword(password)) {
            showMessage("@password.change.dialog.current.password.incorrect");
        } else if (password.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            showMessage("@password.change.dialog.required.fields");
        } else if (!password1.equals(password2)) {
            showMessage("@password.change.dialog.confirm.passwords");
        } else {
            try {
                String result = securityService.changePassword(password, password1);
                
                if (result != null && !result.isEmpty()) {
                    showMessage(result);
                } else if (forced) {
                    String inst = user.getSecurityDomain().getLogicalId();
                    txtUsername.setValue(inst + "\\" + user.getLoginName());
                    EventUtil.send("submit", panel.getPage(), null);
                } else {
                    doCancel();
                    PromptDialog.showInfo("@password.change.dialog.password.changed",
                        "@password.change.dialog.password.changed.dialog.title");
                }
            } catch (Exception e) {
                Throwable e1 = e.getCause() == null ? e : e.getCause();
                showMessage("@password.change.dialog.password.change.error", e1.getMessage());
            }
        }
        txtPassword.setValue("");
        txtPassword1.setValue("");
        txtPassword2.setValue("");
        txtPassword.setFocus(true);
    }
    
    /**
     * Displays the specified message text on the form.
     * 
     * @param text Message text to display.
     * @param args Additional args for message.
     */
    private void showMessage(String text, Object... args) {
        text = StrUtil.formatMessage(text, args);
        lblMessage.setLabel(text);
    }
    
    /**
     * Sets the security service.
     * 
     * @param securityService The security service.
     */
    public void setSecurityService(ISecurityService securityService) {
        this.securityService = securityService;
    }
    
}
