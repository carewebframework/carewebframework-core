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
