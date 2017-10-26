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
package org.carewebframework.maven.plugin.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Represents a config file template.
 */
public class ConfigTemplate {

    private static class ConfigEntry {

        String template;

        int placeholder;

        List<String> buffer = new ArrayList<>();

        ConfigEntry(String template, int placeholder) {
            this.template = template;
            this.placeholder = placeholder;
        }
    }

    private final List<String> buffer = new ArrayList<>();

    private final Map<String, ConfigEntry> entries = new LinkedHashMap<>();

    private final String filename;

    /**
     * Creates a config file template from a classpath resource.
     *
     * @param filename Path to config file template.
     * @throws MojoExecutionException Error loading the template.
     */
    ConfigTemplate(String filename) throws MojoExecutionException {
        this.filename = filename;
        
        try (InputStream in = getClass().getResourceAsStream("/" + filename)) {
            if (in == null) {
                throw new MojoExecutionException("Cannot find config file template: " + filename);
            }

            LineIterator lines = IOUtils.lineIterator(in, "UTF-8");

            while (lines.hasNext()) {
                String line = lines.next();

                if (line.startsWith("*")) {
                    String[] pcs = line.split("\\*", 3);
                    entries.put(pcs[1], new ConfigEntry(pcs[2], buffer.size()));
                    buffer.add("");
                } else {
                    buffer.add(line);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error while creating configuration file.", e);
        }

        if (entries.isEmpty()) {
            throw new MojoExecutionException("Failed to locate insertion point in configuration file.");
        }
    }

    /**
     * Adds an entry to the config file.
     *
     * @param placeholder Placeholder identifying insertion point for entry.
     * @param params Parameters to be applied to the template.
     */
    public void addEntry(String placeholder, String... params) {
        ConfigEntry entry = entries.get(placeholder);
        String line = entry.template;

        for (int i = 0; i < params.length; i++) {
            line = line.replace("{" + i + "}", StringUtils.defaultString(params[i]));
        }

        entry.buffer.add(line);
    }

    /**
     * Create the xml configuration descriptor.
     *
     * @param stagingDirectory The parent directory where the configuration descriptor is to be
     *            created.
     * @throws MojoExecutionException Unspecified exception.
     */
    protected void createFile(File stagingDirectory) throws MojoExecutionException {
        File targetDirectory = newSubdirectory(stagingDirectory, "META-INF");
        File newEntry = new File(targetDirectory, filename);

        try (FileOutputStream out = new FileOutputStream(newEntry); PrintStream ps = new PrintStream(out);) {
            
            Iterator<ConfigEntry> iter = entries.values().iterator();
            ConfigEntry entry = null;

            for (int i = 0; i < buffer.size(); i++) {
                if (entry == null && iter.hasNext()) {
                    entry = iter.next();
                }

                if (entry != null && entry.placeholder == i) {
                    for (String line : entry.buffer) {
                        ps.println(line);
                    }

                    entry = null;
                    continue;
                }

                ps.println(buffer.get(i));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error while creating configuration file.", e);
        }
    }

    /**
     * Creates a new subdirectory under the specified parent directory.
     *
     * @param parentDirectory The directory under which the subdirectory will be created.
     * @param path The full path of the subdirectory.
     * @return The subdirectory just created.
     */
    private File newSubdirectory(File parentDirectory, String path) {
        File dir = new File(parentDirectory, path);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

}
