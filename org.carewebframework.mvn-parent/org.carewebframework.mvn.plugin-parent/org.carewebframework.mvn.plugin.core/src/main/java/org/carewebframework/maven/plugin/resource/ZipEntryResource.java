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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Represents a resource entry within a zip file.
 */
public class ZipEntryResource implements IResource {
    
    private final ZipFile zipFile;
    
    private final ZipEntry zipEntry;
    
    public ZipEntryResource(ZipFile zipFile, ZipEntry zipEntry) {
        this.zipFile = zipFile;
        this.zipEntry = zipEntry;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return zipFile.getInputStream(zipEntry);
    }
    
    @Override
    public String getRelativePath() {
        return zipEntry.getName();
    }
    
    @Override
    public long getTime() {
        return zipEntry.getTime();
    }
    
    @Override
    public boolean isDirectory() {
        return zipEntry.isDirectory();
    }
    
}
