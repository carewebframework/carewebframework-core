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
import java.util.List;
import java.util.Map;

/**
 * Copies additional theme resources without special processing.
 */
class ThemeGeneratorResource extends ThemeGeneratorBase {
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorResource(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        super(theme, mojo);
    }
    
    @Override
    protected void registerProcessors(Map<String, ResourceProcessor> processors) {
        ResourceProcessor processor = new CopyProcessor();
        processors.put("", processor);
    }
    
    @Override
    protected String relocateResource(String resourceName) {
        return "web/" + getResourceBase() + "/" + resourceName;
    }
    
    @Override
    protected String getResourceBase() {
        return mojo.getThemeBase();
    }
    
    @Override
    public void process() throws Exception {
        List<String> resources = mojo.getResources();
        
        if (resources != null && !resources.isEmpty()) {
            mojo.getLog().info("Copying additional resources.");
            
            for (String resource : resources) {
                File src = new File(resource);
                
                if (src.exists()) {
                    processResource(src, null);
                }
            }
        }
    }
    
    private void processResource(File file, String root) throws Exception {
        if (file.isDirectory()) {
            root = root == null ? file.getPath() : root;
            
            for (File f : file.listFiles()) {
                processResource(f, root);
            }
        } else {
            process(new ThemeResourceFile(file, root));
        }
    }
    
}
