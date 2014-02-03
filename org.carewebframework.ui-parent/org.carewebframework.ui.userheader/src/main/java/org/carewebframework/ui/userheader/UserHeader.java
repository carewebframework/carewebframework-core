/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.userheader;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.context.UserContext.IUserContextEvent;
import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.FrameworkWebSupport;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Label;

/**
 * Controller for user header plugin.
 */
public class UserHeader extends PluginController implements IUserContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(UserHeader.class);
    
    private Label userHeader;
    
    private A password;
    
    private static final String DATABASE_DISPLAY_NAME_PROPERTY = "DATABASE.NAME";
    
    private static final String DATABASE_DISPLAY_BACKGROUNDCOLOR_PROPERTY = "DATABASE.BACKGROUNDCOLOR";
    
    private IUser currentUser;
    
    private String dbRegion;
    
    private Component root;
    
    //
    
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
        root = comp;
        dbRegion = StringUtils.trimToEmpty(getPropertyValue(DATABASE_DISPLAY_NAME_PROPERTY));
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
        IInstitution inst = user == null ? null : user.getInstitution();
        
        if (inst != null) {
            text += "@" + inst.getAbbreviation();
        }
        
        HttpServletRequest request = FrameworkWebSupport.getHttpServletRequest();
        String info = StringUtils.trimToEmpty((request == null ? "" : request.getLocalAddr()) + " " + dbRegion);
        userHeader.setValue(text + (info.isEmpty() ? "" : " (" + info + ")"));
        password.setVisible(SecurityUtil.getSecurityService().canChangePassword());
        Clients.resize(root);
    }
    
    /**
     * Returns a property value.
     * 
     * @param propertyName
     * @return
     */
    private String getPropertyValue(final String propertyName) {
        try {
            return PropertyUtil.getValue(propertyName);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IContextEvent#pending(boolean)
     */
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.setColor(getPropertyValue(DATABASE_DISPLAY_BACKGROUNDCOLOR_PROPERTY));
    }
    
}
