/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.iterator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.resource.ZipEntryResource;

/**
 * Used where source archive is a zip (jar) file format.
 */
public class ZipIterator implements IResourceIterator {
    
    private final ZipFile zipFile;
    
    private final Enumeration<? extends ZipEntry> zipEntries;
    
    public ZipIterator(File file) throws ZipException, IOException {
        zipFile = new ZipFile(file);
        zipEntries = zipFile.entries();
    }
    
    public ZipIterator(String file) throws ZipException, IOException {
        zipFile = new ZipFile(file);
        zipEntries = zipFile.entries();
    }
    
    @Override
    public void close() {
        try {
            zipFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean hasNext() {
        return zipEntries.hasMoreElements();
    }
    
    @Override
    public IResource next() {
        return new ZipEntryResource(zipFile, zipEntries.nextElement());
    }
    
    @Override
    public void remove() {
    }
    
}
