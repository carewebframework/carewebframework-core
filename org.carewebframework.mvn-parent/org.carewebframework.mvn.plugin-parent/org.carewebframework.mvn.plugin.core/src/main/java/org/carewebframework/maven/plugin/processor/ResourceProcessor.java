/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.processor;

import java.io.File;
import java.util.List;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.resource.FileResource;
import org.carewebframework.maven.plugin.transform.CopyTransform;

/**
 * Copies additional resources without special processing.
 */
public class ResourceProcessor extends AbstractProcessor<BaseMojo> {
    
    private final String resourceBase;
    
    private final List<String> resources;
    
    /**
     * @param mojo The mojo.
     * @param resourceBase The resource base path.
     * @param resources The resources to process.
     * @throws Exception if error occurs initializing generator
     */
    public ResourceProcessor(BaseMojo mojo, String resourceBase, List<String> resources) throws Exception {
        super(mojo);
        this.resourceBase = resourceBase;
        this.resources = resources;
        registerTransform("*.*", new CopyTransform(mojo));
    }
    
    @Override
    public String relocateResource(String resourceName) {
        return "web/" + getResourceBase() + "/" + resourceName;
    }
    
    @Override
    public String getResourceBase() {
        return resourceBase;
    }
    
    @Override
    public void transform() throws Exception {
        if (resources != null && !resources.isEmpty()) {
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
            root = root == null ? file.getAbsolutePath() : root;
            
            for (File f : file.listFiles()) {
                processResource(f, root);
            }
        } else {
            transform(new FileResource(file, root));
        }
    }
    
}
