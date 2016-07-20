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
package org.carewebframework.maven.plugin.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Represents a CSS resource entry.
 */
public class CSSResource implements IResource {
    
    private final File file;
    
    private final File mapper;
    
    public CSSResource(File file, File mapper) {
        this.file = file;
        this.mapper = mapper;
        
        if (!file.exists()) {
            throw new RuntimeException("Failed to find CSS resource: " + file);
        }
        
        if (mapper != null && !mapper.exists()) {
            throw new RuntimeException("Failed to find mapper file: " + mapper);
        }
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public String getSourcePath() {
        return file.getName();
    }
    
    @Override
    public String getTargetPath() {
        return getSourcePath();
    }
    
    @Override
    public long getTime() {
        return file.lastModified();
    }
    
    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
    public File getMapper() {
        return mapper;
    }
    
}
