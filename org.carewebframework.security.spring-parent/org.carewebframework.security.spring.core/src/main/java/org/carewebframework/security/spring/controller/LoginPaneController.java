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

import java.util.Collection;

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
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller for the login component.
 */
public class LoginPaneController extends GenericForwardComposer<Component> {
    
    private enum DomainSelectionMode {
        ALLOW, DISALLOW, OPTIONAL
    };
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(LoginPaneController.class);
    
    protected static final String DIALOG_LOGIN_PANE = ZKUtil.getResourcePath(LoginPaneController.class) + "loginPane.zul";
    
    protected Listbox lstDomain;
    
    protected Textbox txtUsername;
    
    protected Textbox txtPassword;
    
    private Label lblMessage;
    
    private Label lblStatus;
    
    private Image imgDomain;
    
    private Label lblDomain;
    
    private Component cmpDomainList;
    
    private Component divDomain;
    
    private Component divInfo;
    
    private Label lblHeader;
    
    private Html htmlHeader;
    
    private Label lblInfo;
    
    private Html htmlInfo;
    
    private Component loginPrompts;
    
    private Component loginRoot;
    
    private ISecurityService securityService;
    
    private SavedRequest savedRequest;
    
    private String defaultUsername;
    
    private String defaultPassword;
    
    private String defaultDomain;
    
    private String defaultLogoUrl;
    
    private boolean autoLogin;
    
    /**
     * Initialize the login form.
     *
     * @param comp The root component
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        savedRequest = (SavedRequest) arg.get("savedRequest");
        AuthenticationException authError = (AuthenticationException) arg.get("authError");
        String loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR);//reset back to default
        
        if (LoginWindowController.getException(authError, CredentialsExpiredException.class) != null) {
            loginFailureMessage = Labels.getLabel(Constants.LBL_LOGIN_ERROR_EXPIRED_USER);//override generic UserLoginException default
        } else if (LoginWindowController.getException(authError, DisabledException.class) != null) {
            loginFailureMessage = authError.getMessage();//override generic UserLoginException default
        }
        
        String username = (String) session.removeAttribute(Constants.DEFAULT_USERNAME);
        username = authError == null ? defaultUsername : username;
        showMessage(authError == null ? null : loginFailureMessage);
        txtUsername.setText(username);
        txtPassword.setText(defaultPassword);
        
        if (StringUtils.isEmpty(username)) {
            txtUsername.setFocus(true);
        } else {
            txtPassword.setFocus(true);
        }
        
        Collection<ISecurityDomain> securityDomains = securityService.getSecurityDomains();
        String securityDomainId = securityDomains.size() == 1 ? securityDomains.iterator().next().getLogicalId() : null;
        
        if (StringUtils.isEmpty(securityDomainId)) {
            securityDomainId = (String) session.getAttribute(Constants.DEFAULT_SECURITY_DOMAIN);
        }
        
        if (StringUtils.isEmpty(securityDomainId)) {
            if (savedRequest != null) {
                String params[] = savedRequest.getParameterValues(Constants.DEFAULT_SECURITY_DOMAIN);
                
                if (params != null && params.length > 0) {
                    securityDomainId = params[0];
                }
            } else {
                securityDomainId = execution.getParameter(Constants.DEFAULT_SECURITY_DOMAIN);
            }
        }
        
        if (StringUtils.isEmpty(securityDomainId)) {
            securityDomainId = defaultDomain;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Security domains:" + (securityDomains == null ? "null" : securityDomains.size()));
        }
        
        switch (securityDomains.size()) {
            case 0:
                showStatus(Labels.getLabel(Constants.LBL_LOGIN_NO_VALID_DOMAINS));
                return;
                
            case 1:
                setDomainSelectionMode(DomainSelectionMode.DISALLOW);
                break;
            
            default:
                setDomainSelectionMode(DomainSelectionMode.OPTIONAL);
                break;
        }
        
        for (ISecurityDomain securityDomain : securityDomains) {
            Listitem li = new Listitem();
            li.setValue(securityDomain);
            lstDomain.appendChild(li);
            li.appendChild(new Listcell(securityDomain.getName()));
            
            if (securityDomainId != null && securityDomainId.equals(securityDomain.getLogicalId())) {
                li.setSelected(true);
                securityDomainId = null;
            }
        }
        
        if (lstDomain.getSelectedIndex() == -1) {
            lstDomain.setSelectedIndex(0);
        }
        
        defaultLogoUrl = imgDomain.getSrc();
        domainChanged();
        
        if (authError == null && autoLogin) {
            comp.setVisible(false);
            Events.echoEvent("onSubmit", comp, null);
        }
        
    }
    
    /**
     * Username onOK event handler.
     */
    public void onOK$txtUsername() {
        txtPassword.setFocus(true);
    }
    
    /**
     * Password onOK event handler.
     */
    public void onOK$txtPassword() {
        onSubmit();
    }
    
    /**
     * Authority onSelect event handler.
     */
    public void onSelect$lstDomain() {
        domainChanged();
        txtUsername.setFocus(true);
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
        setDomainSelectionMode(DomainSelectionMode.ALLOW);
    }
    
    /**
     * Enables/disables selection of the domain.
     */
    private void setDomainSelectionMode(DomainSelectionMode mode) {
        cmpDomainList.setVisible(mode == DomainSelectionMode.ALLOW);
        divDomain.setVisible(mode == DomainSelectionMode.OPTIONAL);
    }
    
    /**
     * Returns the selected security domain, if any.
     *
     * @return An ISecurityDomain object. May be null.
     */
    protected ISecurityDomain getSelectedSecurityDomain() {
        Listitem item = lstDomain.getSelectedItem();
        return item == null ? null : (ISecurityDomain) item.getValue();
    }
    
    /**
     * Submits the authentication request.
     */
    public void onSubmit() {
        showMessage("");
        final ISecurityDomain securityDomain = getSelectedSecurityDomain();
        String securityDomainId = securityDomain == null ? null : securityDomain.getLogicalId();
        String username = txtUsername.getValue().trim();
        final String password = txtPassword.getValue();
        
        if (username.contains("\\")) {
            String[] pcs = username.split("\\\\", 2);
            securityDomainId = pcs[0];
            username = pcs[1];
        }
        
        if (!username.isEmpty() && !password.isEmpty() && !securityDomainId.isEmpty()) {
            session.setAttribute(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            FrameworkWebSupport.setCookie(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            session.setAttribute(Constants.DEFAULT_USERNAME, username);
            txtUsername.setValue(securityDomainId + "\\" + username);
            showStatus(Labels.getLabel(Constants.LBL_LOGIN_PROGRESS));
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
     * @param text Status text to display.
     */
    private void showStatus(final String text) {
        lblStatus.setValue(text);
        loginPrompts.setVisible(false);
        lblStatus.setVisible(true);
    }
    
    /**
     * Update dependent UI elements when security domain selection has changed.
     */
    private void domainChanged() {
        ISecurityDomain securityDomain = getSelectedSecurityDomain();
        lblDomain.setValue(securityDomain.getName());
        String logoUrl = securityDomain.getAttribute(Constants.PROP_LOGIN_LOGO);
        imgDomain.setSrc(logoUrl == null ? defaultLogoUrl : logoUrl);
        setMessageText(securityDomain.getAttribute(Constants.PROP_LOGIN_HEADER), lblHeader, htmlHeader, null);
        setMessageText(securityDomain.getAttribute(Constants.PROP_LOGIN_INFO), lblInfo, htmlInfo, divInfo);
    }
    
    /**
     * Sets the message text to the specified value. If the text starts with an html tag, it will be
     * rendered as such.
     *
     * @param value The message text.
     * @param plainText Component to display plain text.
     * @param htmlText Component to display html.
     * @param parent If not null, parent will be hidden if text is empty.
     */
    private void setMessageText(String value, Label plainText, Html htmlText, Component parent) {
        value = StringUtils.trimToEmpty(value);
        boolean isHtml = StringUtils.startsWithIgnoreCase(value, "<html>");
        boolean notEmpty = !value.isEmpty();
        plainText.setVisible(notEmpty && !isHtml);
        htmlText.setVisible(notEmpty && isHtml);
        
        if (parent != null) {
            parent.setVisible(notEmpty);
        }
        
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
     * Sets the default username (for testing/debugging only).
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
    
    /**
     * Sets the default security domain.
     * 
     * @param value Default security domain logical id.
     */
    public void setDefaultDomain(String value) {
        defaultDomain = value;
    }
    
    /**
     * If true, login proceeds without user input.
     * 
     * @param autoLogin True to automatically log in.
     */
    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }
}
