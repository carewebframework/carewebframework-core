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
package org.carewebframework.maven.plugin.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an input resource to be processed.
 */
public interface IResource {
    
    /**
     * Returns an input stream for the resource.
     * 
     * @return An input stream.
     * @throws IOException Exception opening an input stream.
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * Returns the relative path to the source file of this resource.
     * 
     * @return The relative path to the source file.
     */
    String getSourcePath();
    
    /**
     * Returns the relative path to the target file of this resource. This is often the same as the
     * source path. If null is returned, the resource is ignored during processing.
     * 
     * @return The relative path to the target file.
     */
    String getTargetPath();
    
    /**
     * Returns the timestamp of the resource.
     * 
     * @return Resource timestamp.
     */
    long getTime();
    
    /**
     * Returns true if resource is a directory.
     * 
     * @return True if the resource is a directory.
     */
    boolean isDirectory();
}
