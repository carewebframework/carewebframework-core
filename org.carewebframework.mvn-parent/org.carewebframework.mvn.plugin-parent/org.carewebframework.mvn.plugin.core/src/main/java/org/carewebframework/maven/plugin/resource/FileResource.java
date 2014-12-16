/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file resource entry.
 */
public class FileResource implements IResource {
    
    private final File file;
    
    private final String root;
    
    public FileResource(File file, String root) {
        this.file = file;
        this.root = root;
        
        if (!file.exists()) {
            throw new RuntimeException("Failed to find file resource: " + file);
        }
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public String getRelativePath() {
        String path = file.getAbsolutePath();
        
        if (root != null && path.startsWith(root)) {
            path = path.substring(root.length());
        }
        
        return path + (isDirectory() ? "/" : "");
    }
    
    @Override
    public long getTime() {
        return file.lastModified();
    }
    
    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
}
