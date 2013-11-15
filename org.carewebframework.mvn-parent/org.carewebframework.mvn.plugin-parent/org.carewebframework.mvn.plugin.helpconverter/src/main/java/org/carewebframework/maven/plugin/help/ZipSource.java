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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchive;
import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchiveEntry;

/**
 * Used where source archive is a zip (jar) file format.
 */
public class ZipSource implements ISourceArchive {
    
    private class ZipEntryEx implements ISourceArchiveEntry {
        
        private final ZipEntry zipEntry;
        
        public ZipEntryEx(ZipEntry zipEntry) {
            this.zipEntry = zipEntry;
        }
        
        @Override
        public String getRelativePath() {
            return zipEntry.getName();
        }
        
        @Override
        public boolean isDirectory() {
            return zipEntry.isDirectory();
        }
        
        @Override
        public InputStream getInputStream() {
            try {
                return zipFile.getInputStream(zipEntry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private final ZipFile zipFile;
    
    public ZipSource(String file) throws ZipException, IOException {
        zipFile = new ZipFile(file);
    }
    
    @Override
    public Iterator<? extends ISourceArchiveEntry> entries() {
        return new Iterator<ZipEntryEx>() {
            
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            
            @Override
            public boolean hasNext() {
                return zipEntries.hasMoreElements();
            }
            
            @Override
            public ZipEntryEx next() {
                return new ZipEntryEx(zipEntries.nextElement());
            }
            
            @Override
            public void remove() {
            }
        };
    }
    
    @Override
    public void close() {
        try {
            zipFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
