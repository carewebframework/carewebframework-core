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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carewebframework.maven.plugin.resource.FileResource;
import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Used where source archive is a simple folder structure.
 */
public class DirectoryIterator implements IResourceIterator {
    
    private final File root;
    
    private final Iterator<File> iter;
    
    public DirectoryIterator(File root) {
        this.root = root;
        this.iter = listFiles(root, new ArrayList<File>()).iterator();
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
    
    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }
    
    @Override
    public IResource next() {
        return new FileResource(iter.next(), root.getAbsolutePath());
    }
    
    @Override
    public void remove() {
    }
    
}
