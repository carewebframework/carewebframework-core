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
package org.carewebframework.theme;

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
    
    /**
     * Returns singleton instance of the resolver.
     * 
     * @return The theme resolver.
     */
    public static ThemeResolver getInstance() {
        return instance;
    }
    
    /**
     * Set resolver as ZK default.
     */
    private ThemeResolver() {
        ThemeFns.setThemeResolver(this);
    }
    
    /**
     * Attempts current theme resolution by several algorithms in succession.
     */
    @Override
    public String getTheme(HttpServletRequest request) {
        String themeName = null;
        int pass = 0;
        
        do {
            switch (++pass) {
                case 1: // Theme from servlet request.
                    themeName = (String) request.getAttribute(THEME_PROPERTY);
                    break;
                    
                case 2: // Theme from query parameter.
                    themeName = request.getParameter("theme");
                    break;
                    
                case 3: // Theme from session.
                    Desktop dt = FrameworkWebSupport.getDesktop();
                    HttpSession session = request.getSession(false);
                    
                    if (dt != null) {
                        session.removeAttribute(THEME_PROPERTY);
                    } else {
                        themeName = session == null ? null : (String) session.getAttribute(THEME_PROPERTY);
                    }
                    
                    break;
                    
                case 4: // Theme from property.
                    themeName = !PropertyUtil.isAvailable() ? null : PropertyUtil.getValue(THEME_PROPERTY, null);
                    break;
                    
                case 5: // Default theme when all else fails.
                    themeName = "theme-default";
                    break;
            }
        } while (themeName == null || themeName.isEmpty());
        
        if (pass > 1) {
            setTheme(request, null, themeName);
        }
        
        return themeName;
    }
    
    /**
     * Sets the current theme in the servlet request and session.
     */
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
