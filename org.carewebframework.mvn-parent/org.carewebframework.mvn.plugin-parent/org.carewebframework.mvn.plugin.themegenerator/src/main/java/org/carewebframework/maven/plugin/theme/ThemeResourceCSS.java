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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a CSS resource entry.
 */
public class ThemeResourceCSS implements IThemeResource {
    
    private final File file;
    
    public ThemeResourceCSS(File file) {
        this.file = file;
        
        if (!file.exists()) {
            throw new RuntimeException("Failed to find CSS resource: " + file);
        }
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public String getName() {
        return file.getName();
    }
    
    @Override
    public long getTime() {
        return file.lastModified();
    }
    
}
