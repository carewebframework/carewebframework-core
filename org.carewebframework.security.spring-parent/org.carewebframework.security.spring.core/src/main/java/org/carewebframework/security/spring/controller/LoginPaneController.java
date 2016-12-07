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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.Constants;
import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Image;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.core.WebUtil;
import org.carewebframework.web.event.EventUtil;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Controller for the login component.
 */
public class LoginPaneController implements IAutoWired {
    
    private enum DomainSelectionMode {
        ALLOW, DISALLOW, OPTIONAL
    };
    
    private static final long serialVersionUID = 1L;
    
    protected static final String DIALOG_LOGIN_PANE = CWFUtil.getResourcePath(LoginPaneController.class) + "loginPane.cwf";
    
    protected Listbox lstDomain;
    
    protected Textbox txtUsername;
    
    protected Textbox txtPassword;
    
    private Label lblMessage;
    
    private Label lblStatus;
    
    private Image imgDomain;
    
    private Label lblDomain;
    
    private BaseUIComponent cmpDomainList;
    
    private BaseUIComponent divDomain;
    
    private BaseUIComponent divInfo;
    
    private Label lblHeader;
    
    private Html htmlHeader;
    
    private Label lblInfo;
    
    private Html htmlInfo;
    
    private BaseUIComponent loginPrompts;
    
    private BaseUIComponent loginRoot;
    
    private SecurityDomainRegistry securityDomainRegistry;
    
    private SavedRequest savedRequest;
    
    private String defaultUsername;
    
    private String defaultPassword;
    
    private String defaultDomain;
    
    private String defaultLogoUrl;
    
    private boolean autoLogin;
    
    private BaseUIComponent pane;
    
    /**
     * Initialize the login form.
     *
     * @param comp The root component
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        pane = (BaseUIComponent) comp;
        savedRequest = (SavedRequest) comp.getAttribute("savedRequest");
        AuthenticationException authError = (AuthenticationException) comp.getAttribute("authError");
        String loginFailureMessage = StrUtil.getLabel(Constants.LBL_LOGIN_ERROR);//reset back to default
        
        if (LoginWindowController.getException(authError, CredentialsExpiredException.class) != null) {
            loginFailureMessage = StrUtil.getLabel(Constants.LBL_LOGIN_ERROR_EXPIRED_USER);//override generic UserLoginException default
        } else if (LoginWindowController.getException(authError, DisabledException.class) != null) {
            loginFailureMessage = authError.getMessage();//override generic UserLoginException default
        }
        
        String username = null; //(String) session.removeAttribute(Constants.DEFAULT_USERNAME);
        username = authError == null ? defaultUsername : username;
        showMessage(authError == null ? null : loginFailureMessage);
        txtUsername.setValue(username);
        txtPassword.setValue(defaultPassword);
        
        if (StringUtils.isEmpty(username)) {
            txtUsername.setFocus(true);
        } else {
            txtPassword.setFocus(true);
        }
        
        List<ISecurityDomain> securityDomains = new ArrayList<>(securityDomainRegistry.getAll());
        Collections.sort(securityDomains, new Comparator<ISecurityDomain>() {
            
            @Override
            public int compare(ISecurityDomain sd1, ISecurityDomain sd2) {
                return sd1.getName().compareToIgnoreCase(sd2.getName());
            }
            
        });
        
        String securityDomainId = securityDomains.size() == 1 ? securityDomains.get(0).getLogicalId() : null;
        
        if (StringUtils.isEmpty(securityDomainId)) {
            //securityDomainId = (String) session.getAttribute(Constants.DEFAULT_SECURITY_DOMAIN);
        }
        
        if (StringUtils.isEmpty(securityDomainId)) {
            if (savedRequest != null) {
                String params[] = savedRequest.getParameterValues(Constants.DEFAULT_SECURITY_DOMAIN);
                
                if (params != null && params.length > 0) {
                    securityDomainId = params[0];
                }
            } else {
                //securityDomainId = execution.getParameter(Constants.DEFAULT_SECURITY_DOMAIN);
            }
        }
        
        if (StringUtils.isEmpty(securityDomainId)) {
            securityDomainId = defaultDomain;
        }
        
        switch (securityDomains.size()) {
            case 0:
                showStatus(StrUtil.getLabel(Constants.LBL_LOGIN_NO_VALID_DOMAINS));
                return;
            
            case 1:
                setDomainSelectionMode(DomainSelectionMode.DISALLOW);
                break;
            
            default:
                setDomainSelectionMode(DomainSelectionMode.OPTIONAL);
                break;
        }
        
        boolean defaultSet = false;
        
        for (ISecurityDomain securityDomain : securityDomains) {
            Listitem li = new Listitem();
            li.setData(securityDomain);
            lstDomain.addChild(li);
            li.addChild(new Cell(securityDomain.getName()));
            
            if (!defaultSet) {
                if ((securityDomainId != null && securityDomainId.equals(securityDomain.getLogicalId()))
                        || (securityDomainId == null && securityDomain.getAttribute("default") != null)) {
                    li.setSelected(true);
                    defaultSet = true;
                }
            }
        }
        
        if (lstDomain.getSelectedIndex() == -1) {
            lstDomain.setSelectedIndex(0);
        }
        
        defaultLogoUrl = imgDomain.getSrc();
        domainChanged();
        
        if (authError == null && autoLogin) {
            // Do not use setVisible here as it prevents posting of credentials with some versions of ZK.
            ((BaseUIComponent) comp).addStyle("display", "none");
            EventUtil.post("onSubmit", comp, null);
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
        return item == null ? null : (ISecurityDomain) item.getData();
    }
    
    /**
     * Submits the authentication request.
     */
    public void onSubmit() {
        showMessage("");
        ISecurityDomain securityDomain = getSelectedSecurityDomain();
        String securityDomainId = securityDomain == null ? null : securityDomain.getLogicalId();
        String username = txtUsername.getValue().trim();
        String password = txtPassword.getValue();
        
        if (username.contains("\\")) {
            String[] pcs = username.split("\\\\", 2);
            securityDomainId = pcs[0];
            username = pcs[1];
        }
        
        if (!username.isEmpty() && !password.isEmpty() && !securityDomainId.isEmpty()) {
            //session.setAttribute(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            WebUtil.setCookie(Constants.DEFAULT_SECURITY_DOMAIN, securityDomainId);
            //session.setAttribute(Constants.DEFAULT_USERNAME, username);
            txtUsername.setValue(securityDomainId + "\\" + username);
            showStatus(StrUtil.getLabel(Constants.LBL_LOGIN_PROGRESS));
            //session.setAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST, savedRequest);
            EventUtil.send("onSubmit", loginRoot.getPage(), null);
        } else {
            showMessage(StrUtil.getLabel(Constants.LBL_LOGIN_REQUIRED_FIELDS));
            pane.setVisible(true);
        }
    }
    
    /**
     * Displays the specified message text on the form.
     *
     * @param text Message text to display.
     */
    private void showMessage(String text) {
        lblMessage.setLabel(text);
        ((BaseUIComponent) lblMessage.getParent()).setVisible(!StringUtils.isEmpty(text));
    }
    
    /**
     * Disable all user input elements.
     *
     * @param text Status text to display.
     */
    private void showStatus(String text) {
        lblStatus.setLabel(text);
        loginPrompts.setVisible(false);
        lblStatus.setVisible(true);
    }
    
    /**
     * Update dependent UI elements when security domain selection has changed.
     */
    private void domainChanged() {
        ISecurityDomain securityDomain = getSelectedSecurityDomain();
        lblDomain.setData(securityDomain.getName());
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
    private void setMessageText(String value, Label plainText, Html htmlText, BaseUIComponent parent) {
        value = StringUtils.trimToEmpty(value);
        boolean isHtml = StringUtils.startsWithIgnoreCase(value, "<html>");
        boolean notEmpty = !value.isEmpty();
        plainText.setVisible(notEmpty && !isHtml);
        //htmlText.setVisible(notEmpty && isHtml);
        
        if (parent != null) {
            parent.setVisible(notEmpty);
        }
        
        if (isHtml) {
            htmlText.setContent(value);
        } else {
            plainText.setLabel(value);
        }
    }
    
    /**
     * Sets the security domain registry.
     *
     * @param securityDomainRegistry SecurityDomainRegistry implementation
     */
    public void setSecurityDomainRegistry(SecurityDomainRegistry securityDomainRegistry) {
        this.securityDomainRegistry = securityDomainRegistry;
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
