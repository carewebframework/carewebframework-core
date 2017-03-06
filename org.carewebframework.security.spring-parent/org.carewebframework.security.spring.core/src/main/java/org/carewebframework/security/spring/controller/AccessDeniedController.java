package org.carewebframework.security.spring.controller;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
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
            model.addAttribute("message", "You do not have access to the requested resource.");
        } else {
            model.addAttribute("message", user.getLoginName() + " does not have access to the requested resource.");
        }

        return "classpath:/web/org/carewebframework/security/spring/accessDenied.htm";
    }

}
