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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;

/**
 * Controller for the password change dialog.
 */
public class PasswordChangeController implements IAutoWired {
    
    private static final String DIALOG = CWFUtil.getResourcePath(PasswordChangeController.class, 1) + "passwordChange.cwf";
    
    private final String MESSAGE_PASSWORD_RULES = StrUtil.getLabel("security.password.rules.label");
    
    private Window window;
    
    @WiredComponent
    private Textbox txtPassword1;
    
    @WiredComponent
    private Textbox txtPassword2;
    
    @WiredComponent
    private Label lblInfo;
    
    @WiredComponent
    private Label lblMessage;
    
    private IUser user;
    
    private ISecurityService securityService;
    
    public static void show() {
        DialogUtil.popup(DIALOG);
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        window = (Window) comp;
        user = UserContext.getActiveUser();
        
        if (user == null) {
            window.close();
        } else {
            window.setTitle(StrUtil.getLabel("security.password.dialog.panel.title") + " - " + user.getFullName());
            lblInfo.setLabel(StrUtil.getLabel("security.password.dialog.label", MESSAGE_PASSWORD_RULES));
        }
    }
    
    /**
     * Pressing return in the new password text box moves to the confirm password text box.
     */
    @EventHandler(value = "enter", target = "@txtPassword1")
    private void onEnter$txtPassword1() {
        txtPassword2.setFocus(true);
        txtPassword2.selectAll();
    }
    
    /**
     * Pressing return in confirm password text box submits the form.
     */
    @EventHandler(value = "enter", target = "@txtPassword2")
    @EventHandler(value = "click", target = "btnOK")
    private void onEnter$txtPassword2() {
        changePassword();
    }
    
    /**
     * Cancels the form when the Cancel button is clicked.
     */
    @EventHandler(value = "click", target = "btnCancel")
    private void onClick$btnCancel() {
        window.close();
    }
    
    /**
     * Submits the authentication request.
     */
    private void changePassword() {
        showMessage("");
        String password1 = StringUtils.trimToNull(txtPassword1.getValue());
        String password2 = StringUtils.trimToNull(txtPassword2.getValue());
        
        if (password1 == null || password2 == null) {
            (password1 == null ? txtPassword1 : txtPassword2).setFocus(true);
            showMessage("@security.password.dialog.required.fields");
        } else if (!password1.equals(password2)) {
            showMessage("@security.password.dialog.confirm.passwords");
        } else {
            try {
                String result = securityService.changePassword(user.getPassword(), password1);
                
                if (!StringUtils.isEmpty(result)) {
                    showMessage(result);
                } else {
                    window.close();
                    DialogUtil.showInfo("@security.password.dialog.password.changed",
                        "@security.password.dialog.password.changed.dialog.title");
                }
            } catch (Exception e) {
                showMessage("@password.change.dialog.password.change.error", CWFUtil.formatExceptionForDisplay(e));
            }
        }
    }
    
    /**
     * Displays the specified message text on the form.
     *
     * @param text Message text to display.
     * @param args Additional args for message.
     */
    private void showMessage(String text, Object... args) {
        lblMessage.setLabel(StrUtil.formatMessage(text, args));
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
