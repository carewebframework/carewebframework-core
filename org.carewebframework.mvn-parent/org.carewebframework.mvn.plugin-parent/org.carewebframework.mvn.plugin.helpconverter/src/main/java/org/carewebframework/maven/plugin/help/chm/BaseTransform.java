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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.transform.AbstractTransform;

import org.codehaus.plexus.util.StringUtils;

/**
 * Extracts the topic tree from a view, converting from window-1252 to UTF-8 encoding and
 * restructuring to create a more easily parsed format.
 */
public abstract class BaseTransform extends AbstractTransform {
    
    protected static final String UTF8 = "UTF-8";
    
    protected static final Charset CS_UTF8 = Charset.forName(UTF8);
    
    protected static final String WIN1252 = "windows-1252";
    
    protected static final Charset CS_WIN1252 = Charset.forName(WIN1252);
    
    private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8' ?>\n";
    
    protected final String type;
    
    public BaseTransform(BaseMojo mojo, String type) {
        super(mojo);
        this.type = type;
    }
    
    /**
     * Renames the source files.
     */
    @Override
    public String getTargetPath(IResource resource) {
        return type + ".xml";
    }
    
    @Override
    public void transform(IResource resource, OutputStream outputStream) throws Exception {
        write(outputStream, XML_HEADER, true, 0);
        write(outputStream, "<" + type + ">", true, 0);
        super.transform(resource, outputStream);
        write(outputStream, "</" + type + ">", true, 0);
    }
    
    /**
     * Extracts the value of the named attribute.
     * 
     * @param name The attribute name.
     * @param line The source line.
     * @return The attribute value, or null if not found.
     */
    protected String extractAttribute(String name, String line) {
        int i = line.indexOf(name + "=\"");
        i = i == -1 ? i : i + name.length() + 2;
        int j = i == -1 ? -1 : line.indexOf("\"", i);
        return j == -1 ? null : line.substring(i, j);
    }
    
    /**
     * Write the attribute's name/value pair to the output stream.
     * 
     * @param outputStream The output stream.
     * @param name The attribute name.
     * @param value The attribute value.
     */
    protected void writeAttribute(OutputStream outputStream, String name, String value) {
        if (value != null) {
            write(outputStream, " " + name + "=\"" + value + "\"");
        }
    }
    
    /**
     * Writes a setting's name/value pair to the output stream.
     * 
     * @param outputStream The output stream.
     * @param name The setting name.
     * @param value The setting value.
     * @param level The indent level.
     * @throws IOException Exception while writing to the output stream.
     */
    protected void writeSetting(OutputStream outputStream, String name, Object value, int level) throws IOException {
        write(outputStream, "<" + name + ">" + value + "</" + name + ">", true, level);
    }
    
    /**
     * Write data to the output stream without any special formatting.
     * 
     * @param outputStream The output stream.
     * @param data The data to write.
     */
    protected void write(OutputStream outputStream, String data) {
        write(outputStream, data, false, 0);
    }
    
    /**
     * Write data to the output stream with the specified formatting.
     * 
     * @param outputStream The output stream.
     * @param data The data to write.
     * @param terminate If true, add a line terminator.
     * @param level The indent level.
     */
    protected void write(OutputStream outputStream, String data, boolean terminate, int level) {
        try {
            if (data != null) {
                if (level > 0) {
                    outputStream.write(StringUtils.repeat("  ", level).getBytes(CS_UTF8));
                }
                
                outputStream.write(data.getBytes(CS_UTF8));
                
                if (terminate) {
                    outputStream.write("\n".getBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
