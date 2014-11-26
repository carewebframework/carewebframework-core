/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.theme;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a resource file within a theme.
 */
public interface IThemeResource {
    
    /**
     * Returns an input stream for the resource.
     * 
     * @return An input stream.
     * @throws IOException Exception opening an input stream.
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * Returns the name of the resource.
     * 
     * @return The resource name.
     */
    String getName();
    
    /**
     * Returns the timestamp of the resource.
     * 
     * @return Resource timestamp.
     */
    long getTime();
}
