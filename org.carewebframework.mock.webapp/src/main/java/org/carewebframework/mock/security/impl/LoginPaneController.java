/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.mock.security.impl;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

/**
 * Controller for the login component.
 * 
 * @author dmartin
 */
public class LoginPaneController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    protected static final String DIALOG_LOGIN_PANE = ZKUtil.getResourcePath(LoginPaneController.class) + "loginPane.zul";
    
    private Textbox j_username;
    
    private Textbox j_password;
    
    private Label lblMessage;
    
    private Label lblState;
    
    private Label lblFooterText;
    
    private Html htmlFooterText;
    
    private Component loginPrompts;
    
    private Component loginRoot;
    
    private SavedRequest savedRequest;
    
    /**
     * Initialize the login form.
     * 
     * @param comp The root component
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        savedRequest = (SavedRequest) arg.get("savedRequest");
        final AuthenticationException authError = (AuthenticationException) arg.get("authError");
        
        String loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR);//reset back to default
        
        if (authError != null && authError.getCause() instanceof CredentialsExpiredException) {
            loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR_EXPIRED_USER);//override generic UserLoginException default
        }
        
        String username = (String) session.removeAttribute(Constants.DEFAULT_USERNAME);
        username = authError == null ? "" : username;
        showMessage(authError == null ? null : loginFailureMessage);
        j_username.setText(username);
        
        if (StringUtils.isEmpty(username)) {
            j_username.setFocus(true);
        } else {
            j_password.setFocus(true);
        }
        setFooterText(PropertyUtil.getValue("mock.greeting", null));
    }
    
    /**
     * Username onOK event handler.
     */
    public void onOK$j_username() {
        j_password.setFocus(true);
    }
    
    /**
     * Password onOK event handler.
     */
    public void onOK$j_password() {
        doSubmit();
    }
    
    /**
     * Authority onSelect event handler.
     */
    public void onSelect$j_authority() {
        j_username.setFocus(true);
    }
    
    /**
     * Login button onClick handler.
     */
    public void onClick$btnLogin() {
        doSubmit();
    }
    
    /**
     * Submits the authentication request.
     */
    private void doSubmit() {
        showMessage("");
        String instId = PropertyUtil.getValue("mock.authority", null);
        String username = j_username.getValue().trim();
        final String password = j_password.getValue();
        
        if (username.contains("\\")) {
            String[] pcs = username.split("\\\\", 2);
            instId = pcs[0];
            username = pcs[1];
        }
        
        if (!username.isEmpty() && !password.isEmpty() && !instId.isEmpty()) {
            session.setAttribute(Constants.DEFAULT_INSTITUTION, instId);
            FrameworkWebSupport.setCookie(Constants.DEFAULT_INSTITUTION, instId);
            session.setAttribute(Constants.DEFAULT_USERNAME, username);
            //FrameworkWebSupport.setCookie(Constants.DEFAULT_USERNAME, username);
            j_username.setValue(instId + "\\" + username);
            showState(Labels.getLabel(Constants.LBL_LOGIN_PROGRESS));
            session.setAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST, savedRequest);
            Events.sendEvent("onSubmit", loginRoot.getRoot(), null);
        } else {
            showMessage(Labels.getLabel(Constants.LBL_LOGIN_REQUIRED_FIELDS));
        }
    }
    
    /**
     * Displays the specified message text on the form.
     * 
     * @param text Message text to display.
     */
    private void showMessage(final String text) {
        lblMessage.setValue(text);
        lblMessage.setVisible(!StringUtils.isEmpty(text));
    }
    
    /**
     * Disable all user input elements.
     * 
     * @param text State text to display.
     */
    private void showState(final String text) {
        lblState.setValue(text);
        loginPrompts.setVisible(false);
        lblState.setVisible(true);
    }
    
    /**
     * Sets the message text to the specified value. If the text starts with an html tag, it will be
     * rendered as such.
     * 
     * @param value The message text.
     * @param plainText Component to display plain text.
     * @param htmlText Component to display html.
     */
    private void setMessageText(String value, Label plainText, Html htmlText) {
        value = StringUtils.trimToEmpty(value);
        final boolean isHtml = StringUtils.startsWithIgnoreCase(value, "<html>");
        final boolean notEmpty = !value.isEmpty();
        plainText.setVisible(notEmpty && !isHtml);
        htmlText.setVisible(notEmpty && isHtml);
        
        if (isHtml) {
            htmlText.setContent(value);
        } else {
            plainText.setValue(value);
        }
    }
    
    /**
     * Sets the footer message text to the specified value. If the text starts with an html tag, it
     * will be rendered as such.
     * 
     * @param value Footer message text.
     */
    private void setFooterText(String value) {
        setMessageText(value, lblFooterText, htmlFooterText);
    }
    
}
