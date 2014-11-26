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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents a resource entry within a jar file.
 */
public class ThemeResourceJarEntry implements IThemeResource {
    
    private final JarFile jarFile;
    
    private final JarEntry jarEntry;
    
    public ThemeResourceJarEntry(JarFile jarFile, JarEntry jarEntry) {
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return jarFile.getInputStream(jarEntry);
    }
    
    @Override
    public String getName() {
        return jarEntry.getName();
    }
    
    @Override
    public long getTime() {
        return jarEntry.getTime();
    }
    
}
