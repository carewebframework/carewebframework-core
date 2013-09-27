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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.ui.util.RequestUtil;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zul.theme.Themes;

/**
 * Implements theme support.
 */
public class ThemeProvider implements org.zkoss.zk.ui.util.ThemeProvider {
    
    /**
     * Database property constant
     */
    public static final String THEME_PROPERTY = "CAREWEB.THEME";
    
    private final ThemeRegistry themeRegistry;
    
    /**
     * No-arg constructor
     */
    public ThemeProvider() {
        themeRegistry = ThemeRegistry.getInstance();
    }
    
    // Interface: ThemeProvider
    
    /**
     * @see org.zkoss.zk.ui.util.ThemeProvider#beforeWCS(org.zkoss.zk.ui.Execution,
     *      java.lang.String)
     */
    @Override
    public String beforeWCS(Execution exec, String uri) {
        return mapURI(exec, uri);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.ThemeProvider#beforeWidgetCSS(org.zkoss.zk.ui.Execution,
     *      java.lang.String)
     */
    @Override
    public String beforeWidgetCSS(Execution exec, String uri) {
        return mapURI(exec, uri);
    }
    
    /**
     * Returns the themed version of the specified uri, if any.
     * 
     * @param exec The current execution context.
     * @param uri The uri whose themed version is sought.
     * @return The themed version of the input uri, or the input uri itself if there is no themed
     *         version.
     */
    private String mapURI(Execution exec, String uri) {
        ThemeDefinition def = getThemeDefinition(exec);
        return def == null ? uri : def.themedURI(uri);
    }
    
    /**
     * @see org.zkoss.zk.ui.util.ThemeProvider#getThemeURIs(org.zkoss.zk.ui.Execution,
     *      java.util.List)
     */
    @Override
    public Collection<Object> getThemeURIs(Execution exec, List<Object> uris) {
        ThemeDefinition def = getThemeDefinition(exec);
        
        if (def == null) {
            return uris;
        }
        
        List<Object> newUris = new ArrayList<Object>();
        
        for (Object uri : uris) {
            if (uri instanceof String) {
                newUris.add(def.themedURI((String) uri));
            } else {
                newUris.add(uri);
            }
        }
        
        return newUris;
    }
    
    /**
     * @see org.zkoss.zk.ui.util.ThemeProvider#getWCSCacheControl(org.zkoss.zk.ui.Execution,
     *      java.lang.String)
     * @return Value of -1 indicates OK to cache.
     */
    @Override
    public int getWCSCacheControl(Execution exec, String uri) {
        return -1;
    }
    
    /**
     * Returns the theme definition in effect, if any. First looks at the query parameter for a
     * theme reference. If not found, looks at the session associated with the execution. Finally,
     * looks at the user preference setting (which it then caches in the session to avoid multiple
     * property lookups).
     * 
     * @param exec The current execution.
     * @return The active theme definition, or null to indicate use default.
     */
    private ThemeDefinition getThemeDefinition(Execution exec) {
        boolean isAuthenticated = SecurityUtil.isAuthenticated();
        String attr1 = sessionAttribute(isAuthenticated);
        String attr2 = sessionAttribute(!isAuthenticated);
        HttpSession session = RequestUtil.getSession((HttpServletRequest) exec.getNativeRequest());
        String themeName = null;
        int pass = 0;
        
        while (StringUtils.isEmpty(themeName)) {
            switch (++pass) {
                case 1:
                    themeName = (String) exec.getAttribute(attr1);
                    break;
                
                case 2:
                    themeName = exec.getParameter("theme");
                    break;
                
                case 3:
                    themeName = session == null ? null : (String) session.getAttribute(attr1);
                    break;
                
                case 4:
                    themeName = !PropertyUtil.isAvailable() ? null : PropertyUtil.getValue(THEME_PROPERTY, null);
                    break;
                
                case 5:
                    themeName = "default";
                    break;
            }
        }
        
        exec.setAttribute(attr1, themeName);
        
        if (session != null) {
            session.setAttribute(attr1, themeName);
            session.removeAttribute(attr2);
        }
        Themes.setTheme(exec, themeName);
        return themeRegistry.get(themeName);
    }
    
    /**
     * Return the name of the session attribute to use to cache theme setting.
     * 
     * @param isAuthenticated True if the session has been authenticated.
     * @return
     */
    private String sessionAttribute(boolean isAuthenticated) {
        return isAuthenticated ? "user-theme" : "default-theme";
    }
}
