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
import org.apache.maven.plugin.logging.Log;

/**
 * Copies additional theme resources without special processing.
 */
class ThemeGeneratorResource extends ThemeGeneratorBase {
    
    /**
     * @param buildDirectory Scratch build directory
     * @param exclusionFilters WildcardFileFilter (i.e. exclude certain files)
     * @param log The logger
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorResource(File buildDirectory, WildcardFileFilter exclusionFilters, Log log) throws Exception {
        
        super(null, buildDirectory, exclusionFilters, log);
    }
    
    @Override
    protected void registerProcessors(Map<String, ResourceProcessor> processors) {
        ResourceProcessor processor = new CopyProcessor();
        processors.put("", processor);
    }
    
    @Override
    protected String getConfigTemplate() {
        return null;
    }
    
    @Override
    protected String getRootPath() {
        return "org/carewebframework/themes/";
    }
    
    @Override
    protected String relocateResource(String resourceName, String rootPath) {
        return "web/" + rootPath + "/" + resourceName;
    }
    
}
