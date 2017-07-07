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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.util.IOUtils;
import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.help.chm.ChmSource.ChmEntry;
import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Base transform for accessing structured data from a binary source.
 */
public abstract class BinaryTransform extends BaseTransform {

    private ChmEntry resource;

    public BinaryTransform(BaseMojo mojo, String type) {
        super(mojo, type);
    }

    @Override
    public void transform(IResource resource, OutputStream outputStream) throws Exception {
        this.resource = (ChmEntry) resource;
        super.transform(resource, outputStream);
    }

    /**
     * Reads a four-byte integer from the input stream.
     *
     * @param inputStream The input stream.
     * @return A four-byte integer value.
     * @throws IOException Exception while reading from input stream.
     */
    protected int readDWord(InputStream inputStream) throws IOException {
        return readWord(inputStream) | readWord(inputStream) << 16;
    }

    /**
     * Reads a two-byte integer from the input stream.
     *
     * @param inputStream The input stream.
     * @return A two-byte integer value.
     * @throws IOException Exception while reading from input stream.
     */
    protected int readWord(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[2];
        boolean success = inputStream.read(bytes) == 2;
        return !success ? -1 : readWord(bytes, 0);
    }

    /**
     * Reads a four-byte integer from a byte array at the specified offset.
     *
     * @param data The source data.
     * @param offset The byte offset.
     * @return A four-byte integer value.
     */
    protected int readDWord(byte[] data, int offset) {
        return readWord(data, offset) | readWord(data, offset + 2) << 16;
    }

    /**
     * Reads a two-byte integer from a byte array at the specified offset.
     *
     * @param data The source data.
     * @param offset The byte offset.
     * @return A two-byte integer value.
     */
    protected int readWord(byte[] data, int offset) {
        int low = data[offset] & 0xff;
        int high = data[offset + 1] & 0xff;
        return high << 8 | low;
    }

    /**
     * Returns a string value from a zero-terminated byte array.
     *
     * @param data The source data.
     * @return A string.
     */
    protected String getString(byte[] data) {
        return getString(data, 0);
    }

    /**
     * Returns a string value from a zero-terminated byte array at the specified offset.
     *
     * @param data The source data.
     * @param offset The byte offset.
     * @return A string.
     */
    protected String getString(byte[] data, int offset) {
        if (offset < 0) {
            return "";
        }

        int i = offset;
        while (data[i++] != 0x00) {
            ;
        }
        return getString(data, offset, i - offset - 1);
    }

    /**
     * Returns a string value from the byte array.
     *
     * @param data The source data.
     * @param offset The byte offset.
     * @param length The string length.
     * @return A string
     */
    protected String getString(byte[] data, int offset, int length) {
        return new String(data, offset, length, CS_WIN1252);
    }

    /**
     * Loads the entire contents of a binary file into a byte array.
     *
     * @param file The path to the binary file.
     * @return The contents of the input file as a byte array
     * @throws IOException Exception while reading from file.
     */
    protected byte[] loadBinaryFile(String file) throws IOException {
        try (InputStream is = resource.getInputStream(file)) {
            return IOUtils.toByteArray(is);
        }
    }

}
