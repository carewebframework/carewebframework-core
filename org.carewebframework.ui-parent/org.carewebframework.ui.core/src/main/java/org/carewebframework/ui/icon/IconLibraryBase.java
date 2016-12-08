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
package org.carewebframework.ui.icon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.spring.SpringUtil;
import org.springframework.core.io.Resource;

/**
 * A base implementation for an icon library.
 */
public class IconLibraryBase implements IIconLibrary {
    
    private static final Log log = LogFactory.getLog(IconLibraryBase.class);
    
    private final String id;
    
    private final String[] dimensions;
    
    private final String resourcePath;
    
    private final String urlPattern;
    
    protected IconLibraryBase(String id, String resourcePath, String dimensions) {
        this(id, resourcePath, dimensions, "%1$s/%2$s/%3$s/%4$s");
    }
    
    protected IconLibraryBase(String id, String resourcePath, String dimensions, String urlPattern) {
        this.id = id;
        this.resourcePath = resourcePath;
        this.dimensions = StringUtils.split(dimensions, ',');
        this.urlPattern = urlPattern;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getIconUrl(String iconName, String dimensions) {
        return formatURL(iconName, dimensions, "web");
    }
    
    @Override
    public List<String> getMatching(String iconName, String dimensions) {
        List<String> urls = new ArrayList<>();
        
        try {
            for (Resource resource : SpringUtil.getResources(formatURL(iconName, dimensions, "classpath:web"))) {
                String path = resource.getURL().getPath();
                int i = path.indexOf(resourcePath);
                urls.add("web/" + path.substring(i));
            }
        } catch (IOException e) {
            log.error("Error enumerating icons.", e);
        }
        
        return urls;
    }
    
    @Override
    public String[] supportedDimensions() {
        return dimensions;
    }
    
    protected String formatURL(String iconName, String dims, String prefix) {
        if (dims == null) {
            if (dimensions != null && dimensions.length > 0) {
                dims = dimensions[0];
            } else {
                dims = "";
            }
        }
        
        return prefix + String.format(urlPattern, resourcePath, id, dims, iconName).replace("//", "/");
    }
    
}
