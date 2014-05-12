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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Each instance of this class defines a complete definition of a CareWeb theme.
 */
public class ThemeDefinition implements ApplicationContextAware {
    
    private static final String URI_PREFIX = "~./";
    
    private String url;
    
    private String id;
    
    private String description;
    
    private String creator;
    
    private String copyright;
    
    private String version;
    
    private String released;
    
    private Set<String> files;
    
    private ThemeRegistry themeRegistry;
    
    public ThemeDefinition() {
        
    }
    
    /**
     * Remove the theme definition from the registry when it is destroyed.
     */
    public void destroy() {
        themeRegistry.unregister(this);
    }
    
    /**
     * Called when the definition is fully instantiated.
     */
    public void init() {
        themeRegistry.register(this);
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        if (url != null && url.startsWith(URI_PREFIX)) {
            url = url.substring(URI_PREFIX.length());
        }
        
        if (url != null && !url.endsWith("/")) {
            url += "/";
        }
        
        this.url = url;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    public String getCopyright() {
        return copyright;
    }
    
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setReleased(String released) {
        this.released = released;
    }
    
    public String getReleased() {
        return released;
    }
    
    /**
     * Maps a resource uri to its themed version if one exists. If no themed version exists, the
     * original uri is returned.
     * 
     * @param uri
     * @return Mapped resource uri.
     */
    public String themedURI(String uri) {
        if (files == null || uri == null || !uri.startsWith(URI_PREFIX)) {
            return uri;
        }
        
        final String mappedUri = uri.substring(URI_PREFIX.length());
        return files.contains(mappedUri) ? ("/zkau/web/" + (StringUtils.isEmpty(version) ? "" : "_zv" + version + "/") + url + mappedUri)
                : uri;
    }
    
    /**
     * Uses the application context to enumerate all of the file resources associated with the root
     * path for the theme.
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        try {
            final String root = "web/" + url;
            final int rootLength = root.length();
            final String wc = "classpath*:" + root + "**";
            final Resource[] resources = appContext.getResources(wc);
            files = new HashSet<String>(resources.length);
            
            for (Resource resource : resources) {
                final String path = resource.getURL().getPath();
                final int i = path.indexOf(root);
                
                if (i > -1) {
                    files.add(path.substring(i + rootLength));
                }
            }
            
        } catch (Exception e) {}
        
    }
    
    public void setThemeRegistry(ThemeRegistry themeRegistry) {
        this.themeRegistry = themeRegistry;
    }
    
    public ThemeRegistry getThemeRegistry() {
        return themeRegistry;
    }
    
}
