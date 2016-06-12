/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Label;

/**
 * Controller for user header plugin.
 */
public class MainController extends PluginController implements IUserContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    private Label userHeader;
    
    private A password;
    
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
    
    /**
     * @see org.carewebframework.ui.FrameworkController#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
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
        
        userHeader.setValue(text);
        password.setVisible(SecurityUtil.getSecurityService().canChangePassword());
        Clients.resize(root);
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#pending(boolean)
     */
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
}
