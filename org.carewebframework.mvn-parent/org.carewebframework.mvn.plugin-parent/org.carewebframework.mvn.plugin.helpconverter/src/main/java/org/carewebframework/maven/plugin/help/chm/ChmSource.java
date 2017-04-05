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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.chm.accessor.DirectoryListingEntry;
import org.apache.tika.parser.chm.core.ChmCommons;
import org.apache.tika.parser.chm.core.ChmExtractor;
import org.carewebframework.maven.plugin.iterator.IResourceIterator;
import org.carewebframework.maven.plugin.resource.IResource;

/**
 * Used where source archive is a chm file format. Uses the Tika CHM extractor to extract files from
 * the source archive.
 */
public class ChmSource implements IResourceIterator {

    /**
     * Wraps a DirectoryListingEntry as an IResource.
     */
    protected class ChmEntry implements IResource, Comparable<ChmEntry> {

        private final boolean isDirectory;

        private final DirectoryListingEntry entry;

        private final String sourcePath;

        private ChmEntry(DirectoryListingEntry entry) throws TikaException {
            String name = entry.getName();
            isDirectory = name.endsWith("/");
            sourcePath = name.startsWith("/") ? name.substring(1) : name;

            // Note: if the extractor would normally skip this entry, tweak its name to force inclusion.
            if (ChmCommons.hasSkip(entry)) {
                entry = new DirectoryListingEntry(entry.getNameLength(), "_" + sourcePath, entry.getEntryType(),
                        entry.getOffset(), entry.getLength());
            }

            this.entry = entry;
        }

        @Override
        public String getSourcePath() {
            return sourcePath;
        }

        /**
         * Need to explicitly translate #SYSTEM target here so that the Spring configuration file is
         * generated correctly.
         */
        @Override
        public String getTargetPath() {
            return "#SYSTEM".equals(sourcePath) ? "helpset.xml" : sourcePath;
        }

        @Override
        public boolean isDirectory() {
            return isDirectory;
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new ByteArrayInputStream(chmExtractor.extractChmEntry(entry));
            } catch (TikaException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int compareTo(ChmEntry tgt) {
            return sourcePath.compareTo(tgt.sourcePath);
        }

        @Override
        public boolean equals(Object tgt) {
            return tgt instanceof ChmEntry && compareTo((ChmEntry) tgt) == 0;
        }

        @Override
        public long getTime() {
            return timestamp;
        }

        /**
         * Retrieves an input stream for a companion resource.
         *
         * @param file Name of the resource file.
         * @return The input stream or null if not found.
         */
        public InputStream getInputStream(String file) {
            ChmEntry resource = findEntry(file);
            return resource == null ? null : resource.getInputStream();
        }
    }

    private final ChmExtractor chmExtractor;

    private final Set<ChmEntry> entries;

    private final Iterator<ChmEntry> iterator;

    private final long timestamp = System.currentTimeMillis();

    /**
     * Wraps a chm file as an IResourceIterator.
     *
     * @param chmFile The chm file.
     */
    public ChmSource(String chmFile) {
        try (InputStream inp = FileUtils.openInputStream(new File(chmFile));) {
            chmExtractor = new ChmExtractor(inp);
            entries = buildEntryList();
            iterator = entries.iterator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the list of archive file entries.
     * 
     * @return The list of all archive file entries.
     * @throws TikaException Unspecified exception.
     */
    private Set<ChmEntry> buildEntryList() throws TikaException {
        Set<ChmEntry> entries = new TreeSet<>();

        for (DirectoryListingEntry entry : chmExtractor.getChmDirList().getDirectoryListingEntryList()) {
            String name = entry.getName();

            if (name.startsWith("/") && !name.equals("/") && !name.startsWith("/$")) {
                entries.add(new ChmEntry(entry));
            }
        }

        return entries;
    }

    /**
     * Returns the chm entry that references the specified file name.
     *
     * @param file A file name.
     * @return The CHMEntry referencing the specified file, or null if not found.
     */
    private ChmEntry findEntry(String file) {
        for (ChmEntry entry : entries) {
            if (file.equals(entry.getSourcePath())) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public IResource next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

}
