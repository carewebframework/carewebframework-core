/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.help.chm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.transform.AbstractTransform;

/**
 * Transforms HTML pages by converting them from window-1252 encoding to UTF-8 and removing the
 * character set metadata declaration. This is necessary because ZK expects UTF-8 encoding for
 * jar-embedded resources.
 */
public class HtmlTransform extends AbstractTransform {
    
    private static final byte[] EOL = "\r\n".getBytes();
    
    public HtmlTransform(BaseMojo mojo) {
        super(mojo);
    }
    
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        boolean metaFound = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, BaseTransform.CS_WIN1252))) {
            
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!metaFound && line.contains("charset=")) {
                    metaFound = true;
                } else {
                    outputStream.write(line.getBytes(BaseTransform.CS_UTF8));
                    outputStream.write(EOL);
                }
            }
        }
    }
    
}
