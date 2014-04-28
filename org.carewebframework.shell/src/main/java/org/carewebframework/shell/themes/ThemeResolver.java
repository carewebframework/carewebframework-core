/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.themes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.ui.FrameworkWebSupport;

import org.zkoss.web.fn.ThemeFns;
import org.zkoss.zk.ui.Desktop;

/**
 * Theme resolver implementation.
 */
public class ThemeResolver implements org.zkoss.web.theme.ThemeResolver {
    
    /**
     * Database property constant
     */
    public static final String THEME_PROPERTY = "CAREWEB.THEME";
    
    private static final ThemeResolver instance = new ThemeResolver();
    
    public static ThemeResolver getInstance() {
        return instance;
    }
    
    private ThemeResolver() {
        ThemeFns.setThemeResolver(this);
    }
    
    @Override
    public String getTheme(HttpServletRequest request) {
        String themeName = null;
        int pass = 0;
        
        do {
            switch (++pass) {
                case 1:
                    themeName = (String) request.getAttribute(THEME_PROPERTY);
                    break;
                
                case 2:
                    themeName = request.getParameter("theme");
                    break;
                
                case 3:
                    Desktop dt = FrameworkWebSupport.getDesktop();
                    HttpSession session = request.getSession(false);
                    
                    if (dt != null) {
                        session.removeAttribute(THEME_PROPERTY);
                    } else {
                        themeName = session == null ? null : (String) session.getAttribute(THEME_PROPERTY);
                    }
                    
                    break;
                
                case 4:
                    themeName = !PropertyUtil.isAvailable() ? null : PropertyUtil.getValue(THEME_PROPERTY, null);
                    break;
                
                case 5:
                    themeName = "default";
                    break;
            }
        } while (themeName == null || themeName.isEmpty());
        
        if (pass > 1) {
            setTheme(request, null, themeName);
        }
        
        return themeName;
    }
    
    @Override
    public void setTheme(HttpServletRequest request, HttpServletResponse response, String themeName) {
        if (themeName != null) {
            request.setAttribute(THEME_PROPERTY, themeName);
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                session.setAttribute(THEME_PROPERTY, themeName);
            }
        }
    }
    
};
