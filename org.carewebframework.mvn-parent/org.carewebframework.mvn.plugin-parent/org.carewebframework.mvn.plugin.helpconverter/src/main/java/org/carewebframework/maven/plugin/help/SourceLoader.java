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

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.carewebframework.maven.plugin.iterator.DirectoryIterator;
import org.carewebframework.maven.plugin.iterator.IResourceIterator;
import org.carewebframework.maven.plugin.transform.CopyTransform;

/**
 * Generic source loader. Used for JavaHelp and OHJ formats. May be extended to support other
 * formats.
 */
public class SourceLoader {
    
    private final String formatSpecifier;
    
    private final Class<? extends IResourceIterator> iteratorClass;
    
    private final String helpSetPattern;
    
    private FileFilter helpSetFilter;
    
    /**
     * Create a source loader.
     * 
     * @param formatSpecifier The unique help format specifier (e.g., "javahelp").
     * @param helpSetPattern The pattern that will be used to identify the main help set file.
     * @param iteratorClass The class that will be used to iterate over files in the source archive.
     */
    public SourceLoader(String formatSpecifier, String helpSetPattern, Class<? extends IResourceIterator> iteratorClass) {
        this.formatSpecifier = formatSpecifier;
        this.helpSetPattern = helpSetPattern;
        this.iteratorClass = iteratorClass;
    }
    
    /**
     * Register all file transforms with the main processor. This is called by the main processor to
     * allow each source loader to register its file transforms. By default, all files are simply
     * copied from the source to the target. Override if additional transforms are needed.
     * 
     * @param processor The main help processor.
     */
    public void registerTransforms(HelpProcessor processor) {
        processor.registerTransform("*", new CopyTransform(processor.getMojo()));
    }
    
    /**
     * Returns the class that will be used to iterate over the contents of a source archive.
     * 
     * @return A resource iterator.
     */
    public Class<? extends IResourceIterator> getIteratorClass() {
        return iteratorClass;
    }
    
    public String getHelpSetPattern() {
        return helpSetPattern;
    }
    
    public String getFormatSpecifier() {
        return formatSpecifier;
    }
    
    /**
     * Returns true if the file name matches the pattern specified for the main help set file.
     * 
     * @param fileName File name to check.
     * @return True if this is the main help set file.
     */
    public boolean isHelpSetFile(String fileName) {
        if (helpSetFilter == null) {
            helpSetFilter = new WildcardFileFilter(helpSetPattern);
        }
        
        return helpSetFilter.accept(new File(fileName));
    }
    
    /**
     * Returns a resource iterator instance for the given archive name.
     * 
     * @param archiveName Name of the archive file.
     * @return A resource iterator instance.
     * @throws Exception Unspecified exception.
     */
    public IResourceIterator load(String archiveName) throws Exception {
        File file = new File(archiveName);
        
        if (file.isDirectory()) {
            return new DirectoryIterator(file);
        }
        
        return iteratorClass.getConstructor(String.class).newInstance(archiveName);
    }
    
}
