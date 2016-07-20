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
package org.carewebframework.maven.plugin.help;

import org.carewebframework.maven.plugin.iterator.IResourceIterator;
import org.carewebframework.maven.plugin.processor.AbstractProcessor;
import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Processor for help content.
 */
public class HelpProcessor extends AbstractProcessor<HelpConverterMojo> {
    
    private String hsFile;
    
    private final SourceLoader loader;
    
    private final IResourceIterator resourceIterator;
    
    public HelpProcessor(HelpConverterMojo mojo, String archiveName, SourceLoader loader) throws Exception {
        super(mojo);
        this.loader = loader;
        this.resourceIterator = loader.load(archiveName);
        loader.registerTransforms(this);
    }
    
    @Override
    public String relocateResource(String resourcePath) {
        return "web/" + getResourceBase() + resourcePath;
    }
    
    @Override
    public String getResourceBase() {
        String locale = mojo.getModuleLocale();
        locale = locale == null || locale.isEmpty() ? "" : ("/" + locale);
        return mojo.getModuleBase() + mojo.getModuleId() + locale + "/";
    }
    
    @Override
    public void transform() throws Exception {
        transform(resourceIterator);
        
        if (hsFile == null) {
            mojo.throwMojoException("Help set definition file not found in source jar.", null);
        }
    }
    
    /**
     * Excludes resources under the META-INF folder and checks the resource against the help set
     * pattern, saving a path reference if it matches.
     */
    @Override
    public boolean transform(IResource resource) throws Exception {
        String path = resource.getSourcePath();
        
        if (path.startsWith("META-INF/")) {
            return false;
        }
        
        if (hsFile == null && loader.isHelpSetFile(path)) {
            hsFile = getResourceBase() + resource.getTargetPath();
        }
        
        return super.transform(resource);
    }
    
    /**
     * Returns the path to the main help set file, or null if one was not found during processing.
     * 
     * @return The path to the main help set file, or null if none was found.
     */
    public String getHelpSetFile() {
        return hsFile;
    }
}
