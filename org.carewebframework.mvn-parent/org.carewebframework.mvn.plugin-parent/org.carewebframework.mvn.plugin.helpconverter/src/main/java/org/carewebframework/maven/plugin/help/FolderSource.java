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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchive;
import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchiveEntry;

/**
 * Used where source archive is a simple folder structure.
 */
public class FolderSource implements ISourceArchive {
    
    private class FileEx implements ISourceArchiveEntry {
        
        private final File file;
        
        public FileEx(File file) {
            this.file = file;
        }
        
        @Override
        public String getRelativePath() {
            return file.getAbsolutePath().substring(rootLength) + (isDirectory() ? "/" : "");
        }
        
        @Override
        public boolean isDirectory() {
            return file.isDirectory();
        }
        
        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private final File root;
    
    private final int rootLength;
    
    public FolderSource(File root) {
        this.root = root;
        rootLength = root.getAbsolutePath().length() + 1;
    }
    
    @Override
    public Iterator<? extends ISourceArchiveEntry> entries() {
        return new Iterator<FileEx>() {
            
            Iterator<File> iter = listFiles(root, new ArrayList<File>()).iterator();
            
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            
            @Override
            public FileEx next() {
                return new FileEx(iter.next());
            }
            
            @Override
            public void remove() {
            }
            
        };
    }
    
    /**
     * Recurse over entire directory subtree, adding entries to the file list.
     * 
     * @param file Parent file.
     * @param files List to receive files.
     * @return The updated file list.
     */
    private List<File> listFiles(File file, List<File> files) {
        File[] children = file.listFiles();
        
        if (children != null) {
            for (File child : children) {
                files.add(child);
                listFiles(child, files);
            }
        }
        
        return files;
    }
    
    @Override
    public void close() {
    }
    
}
