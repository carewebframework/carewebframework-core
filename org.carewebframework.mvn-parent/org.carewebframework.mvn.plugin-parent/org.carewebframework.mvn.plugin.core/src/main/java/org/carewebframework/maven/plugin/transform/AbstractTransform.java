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
package org.carewebframework.maven.plugin.transform;

import java.io.InputStream;
import java.io.OutputStream;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Abstract base class for transforming input resources. Override the abstract process method to
 * implement the logic for processing an input resource.
 */
public abstract class AbstractTransform {
    
    protected final BaseMojo mojo;
    
    public AbstractTransform(BaseMojo mojo) {
        this.mojo = mojo;
    }
    
    /**
     * Override to allow transform to control naming of target path. By default simply returns the
     * target path specified by the resource.
     * 
     * @param resource The resource being processed.
     * @return The resource's target path.
     */
    public String getTargetPath(IResource resource) {
        return resource.getTargetPath();
    }
    
    public void transform(IResource resource, OutputStream outputStream) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            transform(inputStream, outputStream);
        }
    }
    
    /**
     * Transforms data from an input stream, writing it to an output stream.
     * 
     * @param inputStream Input stream containing data to be transformed.
     * @param outputStream Output stream to receive transformed data.
     * @throws Exception Unspecified exception.
     */
    public abstract void transform(InputStream inputStream, OutputStream outputStream) throws Exception;
    
}
