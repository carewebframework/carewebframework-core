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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.security.spring.Constants;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Timer;

/**
 * Controller for the login component.
 */
public class LoginWindowController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private Component loginForm;
    
    private Timer timer;
    
    private SavedRequest savedRequest;
    
    private final String loginPaneUrl;
    
    private final String passwordPaneUrl;
    
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
     * @param comp - The component
     */
    @SuppressWarnings("deprecation")
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        savedRequest = (SavedRequest) session.removeAttribute(org.carewebframework.security.spring.Constants.SAVED_REQUEST);
        final AuthenticationException authError = (AuthenticationException) session
                .removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        CredentialsExpiredException expired = getException(authError, CredentialsExpiredException.class);
        IUser user = expired != null && SecurityUtil.getSecurityService().canChangePassword() ? (IUser) expired
                .getExtraInformation() : null;
        String form = user != null ? passwordPaneUrl : loginPaneUrl;
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("savedRequest", savedRequest);
        args.put("authError", authError);
        args.put("user", user);
        ZKUtil.loadZulPage(form, loginForm, args, this);
        getPage().setTitle(
            Labels.getLabel(user != null ? Constants.LBL_PASSWORD_CHANGE_PAGE_TITLE : Constants.LBL_LOGIN_PAGE_TITLE));
        resetTimer();
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
     * Reset the timer when user types anything.
     */
    public void onChanging$j_username() {
        resetTimer();
    }
    
    /**
     * Reset the timer when user types anything.
     */
    public void onChanging$j_password() {
        resetTimer();
    }
    
    /**
     * Resets the timer when the user types.
     * 
     * @param event The onChanging event.
     */
    public void onChanging$j_password1(Event event) {
        resetTimer();
    }
    
    /**
     * Resets the timer when the user types.
     * 
     * @param event The onChanging event.
     */
    public void onChanging$j_password2(Event event) {
        resetTimer();
    }
    
    /**
     * Reset timer when selected institution changes.
     */
    public void onSelect$j_domain() {
        resetTimer();
    }
    
    /**
     * Invoked when inactivity timeout has occurred.
     */
    public void onTimer$timer() {
        close(Labels.getLabel(Constants.LBL_LOGIN_FORM_TIMEOUT_MESSAGE));
    }
    
    /**
     * Process a form submission request from another controller.
     */
    public void onSubmit() {
        timer.stop();
        timer = null;
        Clients.submitForm(loginForm);
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
            Events.echoEvent("onResetTimer", timer, null);
        }
    }
    
}
