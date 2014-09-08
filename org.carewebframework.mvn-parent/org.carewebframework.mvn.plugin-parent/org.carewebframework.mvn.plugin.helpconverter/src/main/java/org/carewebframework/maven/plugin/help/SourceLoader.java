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
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Definition file for a source archive loader.
 */
public class SourceLoader {
    
    public interface ISourceArchiveEntry {
        
        String getRelativePath();
        
        InputStream getInputStream();
        
        boolean isDirectory();
        
    }
    
    public interface ISourceArchive {
        
        Iterator<? extends ISourceArchiveEntry> entries();
        
        void close();
        
    }
    
    private String formatSpecifier;
    
    private String loaderClass;
    
    private String helpSetPattern;
    
    private FileFilter helpSetFilter;
    
    public SourceLoader() {
        
    }
    
    public SourceLoader(String formatSpecifier, String helpSetPattern, String loaderClass) {
        this.formatSpecifier = formatSpecifier;
        this.helpSetPattern = helpSetPattern;
        this.loaderClass = loaderClass;
    }
    
    public String getLoaderClass() {
        return loaderClass;
    }
    
    public void setLoaderClass(String loaderClass) {
        this.loaderClass = loaderClass;
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
    public ISourceArchive load(String archiveName) throws Exception {
        File file = new File(archiveName);
        
        if (file.isDirectory()) {
            return new FolderSource(file);
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends ISourceArchive> clazz = (Class<? extends ISourceArchive>) Class.forName(loaderClass);
        return clazz.getConstructor(String.class).newInstance(archiveName);
        
    }
    
}
