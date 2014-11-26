/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.theme;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.codehaus.plexus.util.FileUtils;

/**
 * Generates a new theme directly from a CSS file.
 */
class ThemeGeneratorCSS extends ThemeGeneratorBase {
    
    /**
     * @param theme The theme.
     * @param buildDirectory - Scratch build directory
     * @param exclusionFilters - WildcardFileFilter (i.e. exclude certain files)
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorCSS(Theme theme, File buildDirectory, WildcardFileFilter exclusionFilters) throws Exception {
        
        super(theme, buildDirectory, exclusionFilters);
    }
    
    @Override
    protected void registerProcessors(Map<String, ResourceProcessor> processors) {
        processors.put(".css", new CopyProcessor());
    }
    
    @Override
    protected String getConfigTemplate() {
        return "/theme-config-css.xml";
    }
    
    @Override
    protected String getRootPath() {
        return "org/carewebframework/themes/css/";
    }
    
    @Override
    protected String relocateResource(String resourceName, String rootPath) {
        return "web/" + rootPath + "/" + FileUtils.filename(resourceName);
    }
    
}
