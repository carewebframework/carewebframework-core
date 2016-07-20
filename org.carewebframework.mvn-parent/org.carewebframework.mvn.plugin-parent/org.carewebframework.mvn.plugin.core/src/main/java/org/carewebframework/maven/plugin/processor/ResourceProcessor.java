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
package org.carewebframework.maven.plugin.processor;

import java.io.File;
import java.io.FileNotFoundException;
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
                File src = new File(mojo.getMavenProject().getBasedir(), resource);
                
                if (src.exists()) {
                    processResource(src, null);
                } else {
                    throw new FileNotFoundException(src.getPath());
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
