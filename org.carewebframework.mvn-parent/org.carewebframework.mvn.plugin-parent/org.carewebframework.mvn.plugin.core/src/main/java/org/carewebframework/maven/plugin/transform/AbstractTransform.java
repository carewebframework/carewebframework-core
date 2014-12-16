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
    
    protected InputStream inputStream;
    
    protected OutputStream outputStream;
    
    protected BaseMojo mojo;
    
    public AbstractTransform(BaseMojo mojo) {
        this.mojo = mojo;
    }
    
    public void process(IResource resource, OutputStream outputStream) throws Exception {
        this.inputStream = resource.getInputStream();
        this.outputStream = outputStream;
        process();
        this.inputStream.close();
        this.outputStream.close();
    }
    
    public abstract void process() throws Exception;
}
