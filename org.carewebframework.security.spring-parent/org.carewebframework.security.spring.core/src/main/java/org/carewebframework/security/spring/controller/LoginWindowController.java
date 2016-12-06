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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.Constants;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseInputComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Timer;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.page.PageUtil;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Controller for the login component.
 */
public class LoginWindowController implements IAutoWired {
    
    private BaseComponent loginForm;
    
    private Timer timer;
    
    private SavedRequest savedRequest;
    
    private final String loginPaneUrl;
    
    private final String passwordPaneUrl;
    
    private final IEventListener changeListener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            resetTimer();
        }
        
    };
    
    /**
     * If this authentication exception (or its cause) is of the expected type, return it.
     * Otherwise, return null.
     * 
     * @param exc The authentication exception.
     * @param clazz The desired type.
     * @return The original exception or its cause if one of them is of the expected type.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AuthenticationException> T getException(AuthenticationException exc, Class<T> clazz) {
        if (exc != null) {
            if (clazz.isInstance(exc)) {
                return (T) exc;
            } else if (clazz.isInstance(exc.getCause())) {
                return (T) exc.getCause();
            }
        }
        return null;
    }
    
    public LoginWindowController(String loginPaneUrl, String passwordPaneUrl) {
        super();
        this.loginPaneUrl = loginPaneUrl;
        this.passwordPaneUrl = passwordPaneUrl;
    }
    
    /**
     * Initialize the login form.
     * 
     * @param comp The top level component.
     */
    @Override
    public void afterInitialized(BaseComponent root) {
        timer.setDelay(execution.getSession().getMaxInactiveInterval() * 500);
        savedRequest = (SavedRequest) session.removeAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST);
        AuthenticationException authError = (AuthenticationException) session
                .removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        IUser user = (IUser) session.removeAttribute(org.carewebframework.security.spring.Constants.SAVED_USER);
        Map<String, Object> args = new HashMap<>();
        args.put("savedRequest", savedRequest);
        args.put("authError", authError);
        String form;
        String title;
        
        if (user != null && authError instanceof CredentialsExpiredException
                && SecurityUtil.getSecurityService().canChangePassword()) {
            args.put("user", user);
            form = passwordPaneUrl;
            title = Constants.LBL_PASSWORD_CHANGE_PAGE_TITLE;
        } else {
            form = loginPaneUrl;
            title = Constants.LBL_LOGIN_PAGE_TITLE;
        }
        
        wireListener(PageUtil.createPage(form, loginForm, args).get(0));
        root.getPage().setTitle(StrUtil.getLabel(title));
        resetTimer();
    }
    
    /**
     * Wire change listener to all input elements of child form.
     * 
     * @param root Root element.
     */
    private void wireListener(BaseComponent root) {
        for (BaseComponent child : root.getChildren()) {
            if (child instanceof Combobox || child instanceof Listbox) {
                child.addEventListener("select", changeListener);
            } else if (child instanceof BaseInputComponent) {
                child.addEventListener("changing", changeListener);
            } else if (child instanceof Button) {
                child.addEventListener("click", changeListener);
            } else {
                wireListener(child);
            }
        }
    }
    
    /**
     * Callback to start the timer. This must be done in a separate execution from the call to
     * timer.stop. Otherwise, the timer will not be reset.
     */
    public void onResetTimer$timer() {
        if (timer != null) {
            timer.start();
        }
    }
    
    /**
     * Invoked when inactivity timeout has occurred.
     */
    public void onTimer$timer() {
        close(StrUtil.getLabel(Constants.LBL_LOGIN_FORM_TIMEOUT_MESSAGE));
    }
    
    /**
     * Process a form submission request from another controller.
     */
    public void onSubmit() {
        timer.stop();
        timer = null;
        ClientUtil.submit(loginForm);
    }
    
    /**
     * Process a close request from another controller.
     * 
     * @param event Close event. Data field contains message to display.
     */
    public void onClose(Event event) {
        close((String) event.getData());
    }
    
    /**
     * Close the dialog and display the specified message.
     * 
     * @param message The message text.
     */
    private void close(String message) {
        SecurityUtil.getSecurityService().logout(true, savedRequest == null ? null : savedRequest.getRedirectUrl(), message);
    }
    
    /**
     * Restarts the timer.
     */
    private void resetTimer() {
        if (timer != null) {
            timer.stop();
            EventUtil.post("resetTimer", timer, null);
        }
    }
    
}
