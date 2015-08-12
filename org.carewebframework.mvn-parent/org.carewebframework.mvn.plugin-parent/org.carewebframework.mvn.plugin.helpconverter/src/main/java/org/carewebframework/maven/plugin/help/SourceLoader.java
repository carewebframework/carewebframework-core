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

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.carewebframework.maven.plugin.iterator.DirectoryIterator;
import org.carewebframework.maven.plugin.iterator.IResourceIterator;

/**
 * Definition file for a source archive loader.
 */
public class SourceLoader {
    
    private String formatSpecifier;
    
    private String iteratorClass;
    
    private String helpSetPattern;
    
    private FileFilter helpSetFilter;
    
    public SourceLoader() {
    
    }
    
    public SourceLoader(String formatSpecifier, String helpSetPattern, String iteratorClass) {
        this.formatSpecifier = formatSpecifier;
        this.helpSetPattern = helpSetPattern;
        this.iteratorClass = iteratorClass;
    }
    
    public String getIteratorClass() {
        return iteratorClass;
    }
    
    public void setIteratorClass(String iteratorClass) {
        this.iteratorClass = iteratorClass;
    }
    
    public String getHelpSetPattern() {
        return helpSetPattern;
    }
    
    public void setHelpSetPattern(String helpSetPattern) {
        this.helpSetPattern = helpSetPattern;
    }
    
    public String getFormatSpecifier() {
        return formatSpecifier;
    }
    
    public void setFormatSpecifier(String formatSpecifier) {
        this.formatSpecifier = formatSpecifier;
    }
    
    public boolean isHelpSetFile(String fileName) {
        if (helpSetFilter == null) {
            helpSetFilter = new WildcardFileFilter(helpSetPattern);
        }
        
        return helpSetFilter.accept(new File(fileName));
    }
    
    /**
     * Returns an ISourceArchive implementation for the given archive name.
     * 
     * @param archiveName Name of the archive file.
     * @return An ISourceArchive instance.
     * @throws Exception Unspecified exception.
     */
    public IResourceIterator load(String archiveName) throws Exception {
        File file = new File(archiveName);
        
        if (file.isDirectory()) {
            return new DirectoryIterator(file);
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends IResourceIterator> clazz = (Class<? extends IResourceIterator>) Class.forName(iteratorClass);
        return clazz.getConstructor(String.class).newInstance(archiveName);
        
    }
    
    /**
     * Override to provide custom renaming of copied files.
     * 
     * @param fileName Original file name.
     * @return Transformed file name.
     */
    public String transformFileName(String fileName) {
        return fileName;
    }
}
