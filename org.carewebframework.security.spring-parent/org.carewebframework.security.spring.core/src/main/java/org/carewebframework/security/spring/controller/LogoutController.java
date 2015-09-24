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

import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractSecurityService;
import org.carewebframework.security.spring.Constants;
import org.carewebframework.ui.Application;

import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.util.GenericAutowireComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

/**
 * Controller for logout page.
 */
public class LogoutController extends GenericAutowireComposer<HtmlBasedComponent> {
    
    private static final long serialVersionUID = 1L;
    
    private Label lblMessage;
    
    private Button btnLogin;
    
    /**
     * @see org.zkoss.zk.ui.util.GenericAutowireComposer#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(HtmlBasedComponent comp) throws Exception {
        super.doAfterCompose(comp);
        lblMessage.setValue(AbstractSecurityService.getLogoutAttribute(Constants.LOGOUT_WARNING_ATTR,
            StrUtil.getLabel(Constants.LBL_LOGOUT_MESSAGE_DEFAULT)));
        btnLogin.setHref(AbstractSecurityService.getLogoutAttribute(Constants.LOGOUT_TARGET_ATTR, "/"));
        // Unregister this desktop to allow session to auto-invalidate if appropriate.
        Application.getInstance().register(desktop, false);
    }
    
}
