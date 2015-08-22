/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
