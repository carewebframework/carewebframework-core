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
