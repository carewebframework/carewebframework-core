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
