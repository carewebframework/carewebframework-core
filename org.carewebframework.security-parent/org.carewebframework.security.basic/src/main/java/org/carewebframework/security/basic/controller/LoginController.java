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
package org.carewebframework.security.basic.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.common.StrUtil;
import org.carewebframework.security.AbstractSecurityService;
import org.carewebframework.web.client.WebJarLocator;
import org.carewebframework.web.core.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for login page.
 */
@Controller
public class LoginController {

    @Autowired
    private AbstractSecurityService securityService;

    @GetMapping("security/login")
    public String login(ModelMap model, HttpServletRequest request) {
        Collection<ISecurityDomain> domains = SecurityDomainRegistry.getInstance().getAll();
        model.addAttribute("baseUrl", RequestUtil.getBaseURL(request));
        model.addAttribute("webjarInit", WebJarLocator.getInstance().getWebJarInit());
        model.addAttribute("timeout", request.getSession().getMaxInactiveInterval() * 1000);
        model.addAttribute("domainCount", domains.size());
        model.addAttribute("domains", domains);
        model.addAttribute("disabled", securityService.loginDisabled());
        model.addAttribute("action", "security/login");
        
        String error = request.getParameter("error");
        model.addAttribute("error",
            error == null ? null : error.isEmpty() ? StrUtil.getLabel("security.login.error") : error);
        return "classpath:/web/org/carewebframework/security/basic/login.htm";
    }

}
