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

import org.carewebframework.shell.themes.ThemeDefinition.ThemeType;

import org.zkoss.zk.ui.Execution;

/**
 * Implements theme support.
 */
public class ThemeProvider implements org.zkoss.zk.ui.util.ThemeProvider {
    
    private final ThemeRegistry themeRegistry;
    
    private final ThemeResolver themeResolver;
    
    /**
     * No-arg constructor
     */
    public ThemeProvider() {
        themeRegistry = ThemeRegistry.getInstance();
        themeResolver = ThemeResolver.getInstance();
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
        
        if (def.getType() == ThemeType.CSS) {
            newUris.add(def.expandURI(""));
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
     * Returns the theme definition in effect, if any. Obtains the theme name from the theme
     * resolver, then looks it up in the theme registry.
     * 
     * @param exec The current execution.
     * @return The active theme definition, or null to indicate use default.
     */
    private ThemeDefinition getThemeDefinition(Execution exec) {
        String themeName = themeResolver.getTheme((HttpServletRequest) exec.getNativeRequest());
        return themeRegistry.get(themeName);
    }
    
}
