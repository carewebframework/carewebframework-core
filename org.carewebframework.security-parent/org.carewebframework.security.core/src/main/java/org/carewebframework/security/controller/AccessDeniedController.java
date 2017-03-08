package org.carewebframework.security.controller;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for access denied page.
 */
@Controller
public class AccessDeniedController {
    
    @RequestMapping("security/accessDenied")
    public String accessDenied(ModelMap model) {
        IUser user = SecurityUtil.getAuthenticatedUser();
        model.addAttribute("title", "Access Denied");
        model.addAttribute("authenticated", user != null);
        
        if (user == null) {
            model.addAttribute("message", StrUtil.getLabel("security.denied.message.anonymous"));
        } else {
            model.addAttribute("message", StrUtil.getLabel("security.denied.message.authenticated", user.getLoginName()));
        }

        return "classpath:/web/org/carewebframework/security/accessDenied.htm";
    }

}
