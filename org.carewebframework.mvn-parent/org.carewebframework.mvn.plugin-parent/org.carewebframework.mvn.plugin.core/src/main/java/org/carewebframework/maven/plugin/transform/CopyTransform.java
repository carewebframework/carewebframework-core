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

import org.apache.commons.io.IOUtils;

import org.carewebframework.maven.plugin.core.BaseMojo;

/**
 * Performs a simple copy of a resource from the source to the destination.
 */
public class CopyTransform extends AbstractTransform {
    
    public CopyTransform(BaseMojo mojo) {
        super(mojo);
    }
    
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        IOUtils.copy(inputStream, outputStream);
    }
}
