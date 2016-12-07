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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Each instance of this class defines a complete definition of a CareWeb theme.
 */
public class ThemeDefinition implements ApplicationContextAware {
    
    private static final String URI_PREFIX = "web/";
    
    private String url;
    
    private String id;
    
    private String description;
    
    private String creator;
    
    private String copyright;
    
    private String version;
    
    private String released;
    
    private Set<String> files;
    
    public ThemeDefinition() {
        
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
        this.url = url != null && url.startsWith(URI_PREFIX) ? url.substring(URI_PREFIX.length()) : url;
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
     * Uses the application context to enumerate all of the file resources associated with the root
     * path for the theme.
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        try {
            String root = "web/" + url;
            int rootLength = root.length();
            String wc = "classpath*:" + root + "**";
            Resource[] resources = appContext.getResources(wc);
            files = new HashSet<>(resources.length);
            
            for (Resource resource : resources) {
                String path = resource.getURL().getPath();
                int i = path.indexOf(root);
                
                if (i > -1) {
                    files.add(path.substring(i + rootLength));
                }
            }
            
        } catch (Exception e) {}
        
    }
    
}
