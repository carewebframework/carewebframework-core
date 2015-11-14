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
import java.util.Stack;

import org.apache.commons.lang.ArrayUtils;

import org.carewebframework.maven.plugin.core.BaseMojo;

/**
 * Extracts the topic tree from a view, converting from window-1252 to UTF-8 encoding and
 * restructuring to create a more easily parsed format.
 */
public class ViewTransform extends BaseTransform {
    
    private static final String[] TOKENS = { "ul", "/ul", "li", "param", "/object" };
    
    public ViewTransform(BaseMojo mojo, String type) {
        super(mojo, type);
    }
    
    /**
     * Transforms the input to well-formed XML.
     */
    @Override
    public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CS_WIN1252))) {
            String line;
            String closingTag = null;
            Stack<String> stack = new Stack<>();
            String id = null;
            String url = null;
            String label = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                int i = line.indexOf('>', 1);
                int j = line.indexOf(' ', 1);
                int end = i == -1 ? j : j == -1 ? i : i < j ? i : j;
                int token = ArrayUtils.indexOf(TOKENS, end < 1 ? "" : line.substring(1, end).toLowerCase());
                
                if (stack.isEmpty() && token != 0) {
                    continue;
                }
                
                switch (token) {
                    case 0: // <ul>
                        if (closingTag != null) {
                            write(outputStream, ">", true, 0);
                            closingTag = "</topic>";
                        }
                        
                        stack.push(closingTag);
                        closingTag = null;
                        break;
                        
                    case 1: // </ul>
                        write(outputStream, closingTag, true, 0);
                        closingTag = stack.pop();
                        writeClosingTag(outputStream, closingTag, stack.size());
                        closingTag = null;
                        break;
                        
                    case 2: // <li>
                        writeClosingTag(outputStream, closingTag, 0);
                        write(outputStream, "<topic", false, stack.size());
                        closingTag = " />";
                        break;
                        
                    case 3: // <param>
                        String name = extractAttribute("name", line);
                        String value = extractAttribute("value", line);
                        
                        if ("name".equalsIgnoreCase(name)) {
                            if (label == null) {
                                label = value;
                            } else {
                                id = value;
                            }
                        } else if ("local".equalsIgnoreCase(name)) {
                            url = value;
                        }
                        break;
                        
                    case 4: // </object>
                        writeAttribute(outputStream, "id", id);
                        writeAttribute(outputStream, "label", label);
                        writeAttribute(outputStream, "url", url);
                        id = label = url = null;
                        break;
                }
            }
        }
    }
    
    private void writeClosingTag(OutputStream outputStream, String closingTag, int indent) {
        if (closingTag != null) {
            write(outputStream, closingTag, true, closingTag.startsWith("<") ? indent : 0);
        }
    }
}
