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
package org.carewebframework.plugin.userheader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.context.UserContext.IUserContextEvent;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Hyperlink;
import org.carewebframework.web.component.Label;

/**
 * Controller for user header plugin.
 */
public class MainController extends PluginController implements IUserContextEvent {
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    private Label userHeader;
    
    private Hyperlink password;
    
    private IUser currentUser;
    
    /**
     * Event handler for logout link
     */
    public void onClick$logout() {
        CareWebUtil.getShell().logout();
    }
    
    /**
     * Event handler for lock link
     */
    public void onClick$lock() {
        CareWebUtil.getShell().lock();
    }
    
    /**
     * Event handler for change password link
     */
    public void onClick$password() {
        SecurityUtil.getSecurityService().changePassword();
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        committed();
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#canceled()
     */
    @Override
    public void canceled() {
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#committed()
     */
    @Override
    public void committed() {
        IUser user = UserContext.getActiveUser();
        
        if (log.isDebugEnabled()) {
            log.debug("user: " + user);
        }
        
        if (currentUser != null && currentUser.equals(user)) {
            return;
        }
        
        currentUser = user;
        String text = user == null ? "" : user.getFullName();
        
        if (user != null && user.getSecurityDomain() != null) {
            text += "@" + user.getSecurityDomain().getName();
        }
        
        userHeader.setLabel(text);
        password.setVisible(SecurityUtil.getSecurityService().canChangePassword());
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#pending(boolean)
     */
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
}
