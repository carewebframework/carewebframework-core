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

import java.io.InputStream;
import java.io.OutputStream;

import org.carewebframework.maven.plugin.core.BaseMojo;

/**
 * Transforms settings extracted from the #SYSTEM file to a standard properties file.
 */
public class SystemTransform extends BinaryTransform {
    
    public SystemTransform(BaseMojo mojo) {
        super(mojo, "helpset");
    }
    
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        readDWord(inputStream); // version #
        int code;
        
        while ((code = readWord(inputStream)) != -1) {
            int len = readWord(inputStream);
            byte[] data = new byte[len];
            inputStream.read(data);
            
            switch (code) {
                case 0: // Contents file
                    break;
                    
                case 1: // Index file
                    break;
                    
                case 2: // Default topic
                    writeSetting(outputStream, "defaultTopic", getString(data), 1);
                    break;
                    
                case 3: // Title
                    writeSetting(outputStream, "title", getString(data), 1);
                    break;
                    
                case 4: // Settings
                    break;
            }
        }
    }
    
}
