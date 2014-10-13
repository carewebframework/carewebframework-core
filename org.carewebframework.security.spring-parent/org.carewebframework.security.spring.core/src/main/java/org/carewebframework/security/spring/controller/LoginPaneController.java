/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.security.spring.Constants;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for the login component.
 */
public class LoginPaneController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(LoginPaneController.class);
    
    protected static final String DIALOG_LOGIN_PANE = ZKUtil.getResourcePath(LoginPaneController.class) + "loginPane.zul";
    
    protected Listbox j_domain;
    
    protected Textbox j_username;
    
    protected Textbox j_password;
    
    private Label lblMessage;
    
    private Label lblState;
    
    private Label lblDomain;
    
    private Component cmpDomainList;
    
    private Component divDomain;
    
    private Label lblFooterText;
    
    private Html htmlFooterText;
    
    private Component loginPrompts;
    
    private Component loginRoot;
    
    private ISecurityService securityService;
    
    private SavedRequest savedRequest;
    
    private String defaultUsername;
    
    private String defaultPassword;
    
    private String footerText;
    
    /**
     * Initialize the login form.
     *
     * @param comp The root component
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        savedRequest = (SavedRequest) arg.get("savedRequest");
        AuthenticationException authError = getAuthException();
        String loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR);//reset back to default
        
        if (LoginWindowController.getException(authError, CredentialsExpiredException.class) != null) {
            loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR_EXPIRED_USER);//override generic UserLoginException default
        } else if (LoginWindowController.getException(authError, DisabledException.class) != null) {
            loginFailureMessage = authError.getMessage();//override generic UserLoginException default
        }
        
        String username = (String) session.removeAttribute(Constants.DEFAULT_USERNAME);
        username = authError == null ? defaultUsername : username;
        showMessage(authError == null ? null : loginFailureMessage);
        j_username.setText(username);
        j_password.setText(defaultPassword);
        
        if (StringUtils.isEmpty(username)) {
            j_username.setFocus(true);
        } else {
            j_password.setFocus(true);
        }
        
        List<ISecurityDomain> securityDomains = securityService.getSecurityDomains();
        divDomain.setVisible(securityDomains.size() > 1);
        String defaultDomain = securityDomains.size() == 1 ? securityDomains.get(0).getLogicalId() : null;
        
        if (StringUtils.isEmpty(defaultDomain)) {
            defaultDomain = (String) session.getAttribute(Constants.DEFAULT_SECURITY_DOMAIN);
        }
        
        if (StringUtils.isEmpty(defaultDomain)) {
            SavedRequest savedRequest = (SavedRequest) session
                    .getAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST);
            
            if (savedRequest != null) {
                String params[] = savedRequest.getParameterValues(Constants.DEFAULT_SECURITY_DOMAIN);
                
                if (params != null && params.length > 0) {
                    defaultDomain = params[0];
                }
            } else {
                defaultDomain = execution.getParameter(Constants.DEFAULT_SECURITY_DOMAIN);
            }
        }
        
        if (StringUtils.isEmpty(defaultDomain)) {
            allowDomainSelection();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Security domains:" + (securityDomains == null ? "null" : securityDomains.size()));
        }
        
        for (ISecurityDomain securityDomain : securityDomains) {
            Listitem li = new Listitem();
            li.setValue(securityDomain);
            j_domain.appendChild(li);
            li.appendChild(new Listcell(securityDomain.getName()));
            
            if (securityDomain.getLogicalId().equals(defaultDomain)) {
                li.setSelected(true);
            }
        }
        
        if (j_domain.getChildren().size() > 0) {
            if (j_domain.getSelectedIndex() == -1) {
                j_domain.setSelectedIndex(0);
            }
        } else {
            showState(Labels.getLabel(Constants.LBL_LOGIN_NO_VALID_DOMAINS));
        }
        
        setFooterText(getFooterText());
        domainChanged();
        
        if (authError == null && defaultPassword != null) {
            comp.setVisible(false);
            Events.echoEvent("onSubmit", comp, null);
        }
        
    }
    
    /**
     * Returns the cached authentication exception, if any.
     * 
     * @return The cached authentication exception, or null if none.
     */
    protected AuthenticationException getAuthException() {
        return (AuthenticationException) arg.get("authError");
    }
    
    /**
     * Returns footer text.
     * 
     * @return Footer text (may be null).
     */
    protected String getFooterText() {
        return footerText;
    }
    
    /**
     * Sets the footer message text to the specified value. If the text starts with an html tag, it
     * will be rendered as such.
     *
     * @param value Footer message text.
     */
    public void setFooterText(String value) {
        footerText = value;
        
        if (lblFooterText != null) {
            setMessageText(value, lblFooterText, htmlFooterText);
        }
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
        onSubmit();
    }
    
    /**
     * Authority onSelect event handler.
     */
    public void onSelect$j_domain() {
        domainChanged();
        j_username.setFocus(true);
    }
    
    /**
     * Login button onClick handler.
     */
    public void onClick$btnLogin() {
        onSubmit();
    }
    
    /**
     * Enable domain selection.
     */
    public void onClick$btnDomain() {
        allowDomainSelection();
    }
    
    /**
     * Enables selection of the domain.
     */
    private void allowDomainSelection() {
        cmpDomainList.setVisible(true);
        divDomain.setVisible(false);
    }
    
    /**
     * Returns the selected security domain, if any.
     *
     * @return An ISecurityDomain object. May be null.
     */
    protected ISecurityDomain getSelectedSecurityDomain() {
        Listitem item = j_domain.getSelectedItem();
        return item == null ? null : (ISecurityDomain) item.getValue();
    }
    
    /**
     * Submits the authentication request.
     */
    public void onSubmit() {
        showMessage("");
        final ISecurityDomain securityDomain = getSelectedSecurityDomain();
        String securityDomainId = securityDomain == null ? null : securityDomain.getLogicalId();
        String username = j_username.getValue().trim();
        final String password = j_password.getValue();
        
        if (username.contains("\\")) {
            String[] pcs = username.split("\\\\", 2);
            securityDomainId = pcs[0];
            username = pcs[1];
        }
        
        if (!username.isEmpty() && !password.isEmpty() && !securityDomainId.isEmpty()) {
            session.setAttribute(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            FrameworkWebSupport.setCookie(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            session.setAttribute(Constants.DEFAULT_USERNAME, username);
            j_username.setValue(securityDomainId + "\\" + username);
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
    
    private void domainChanged() {
        lblDomain.setValue(getSelectedSecurityDomain().getName());
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
        boolean isHtml = StringUtils.startsWithIgnoreCase(value, "<html>");
        boolean notEmpty = !value.isEmpty();
        plainText.setVisible(notEmpty && !isHtml);
        htmlText.setVisible(notEmpty && isHtml);
        
        if (isHtml) {
            htmlText.setContent(value);
        } else {
            plainText.setValue(value);
        }
    }
    
    /**
     * Sets the security service.
     *
     * @param securityService SecurityService implementation
     */
    public void setSecurityService(ISecurityService securityService) {
        this.securityService = securityService;
    }
    
    /**
     * Sets the default username (for testing/debugging only)
     * 
     * @param value Default username.
     */
    public void setDefaultUsername(String value) {
        defaultUsername = value;
    }
    
    /**
     * Sets the default password (for testing/debugging only)
     * 
     * @param value Default password.
     */
    public void setDefaultPassword(String value) {
        defaultPassword = value;
    }
}
