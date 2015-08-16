/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.help;

import org.carewebframework.maven.plugin.iterator.IResourceIterator;
import org.carewebframework.maven.plugin.processor.AbstractProcessor;
import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.transform.CopyTransform;

public class HelpProcessor extends AbstractProcessor<HelpConverterMojo> {
    
    private String hsFile;
    
    private final SourceLoader loader;
    
    private final IResourceIterator resourceIterator;
    
    public HelpProcessor(HelpConverterMojo mojo, String archiveName, SourceLoader loader) throws Exception {
        super(mojo);
        this.loader = loader;
        this.resourceIterator = loader.load(archiveName);
        registerTransform("*", new CopyTransform(mojo));
    }
    
    @Override
    public String relocateResource(String resourcePath) {
        return "web/" + getResourceBase() + resourcePath;
    }
    
    @Override
    public String getResourceBase() {
        return mojo.getModuleBase() + mojo.getModuleId() + "/";
    }
    
    @Override
    public void transform() throws Exception {
        transform(resourceIterator);
        
        if (hsFile == null) {
            mojo.throwMojoException("Help set definition file not found in source jar.", null);
        }
    }
    
    @Override
    public boolean transform(IResource resource) throws Exception {
        String path = resource.getSourcePath();
        
        if (path.startsWith("META-INF/")) {
            return false;
        }
        
        if (hsFile == null && loader.isHelpSetFile(path)) {
            hsFile = getResourceBase() + path;
        }
        
        return super.transform(resource);
    }
    
    public String getHelpSetFile() {
        return hsFile;
    }
}
