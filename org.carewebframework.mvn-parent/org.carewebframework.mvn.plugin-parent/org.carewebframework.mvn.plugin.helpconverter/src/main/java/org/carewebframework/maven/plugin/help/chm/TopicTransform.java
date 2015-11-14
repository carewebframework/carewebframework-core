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
 * Transforms #TOPICS file to XML format.
 */
public class TopicTransform extends BinaryTransform {
    
    public TopicTransform(BaseMojo mojo) {
        super(mojo, "topics");
    }
    
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        byte[] strings = loadBinaryFile("#STRINGS");
        byte[] urltbl = loadBinaryFile("#URLTBL");
        byte[] urlstr = loadBinaryFile("#URLSTR");
        byte[] data = new byte[16];
        
        while (inputStream.read(data) == 16) {
            String id = getString(strings, readDWord(data, 4));
            String label = id.replace("_no", "").replace("_", " ");
            int urltblOffset = readDWord(data, 8);
            int urlstrOffset = readDWord(urltbl, urltblOffset + 8);
            String url = getString(urlstr, urlstrOffset + 8);
            write(outputStream, "<topic", false, 1);
            writeAttribute(outputStream, "id", id);
            writeAttribute(outputStream, "label", label);
            writeAttribute(outputStream, "url", url);
            write(outputStream, " />", true, 0);
        }
    }
    
}
