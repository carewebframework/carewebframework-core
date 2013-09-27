/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.help;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.carewebframework.maven.plugin.core.BaseMojo;

import org.codehaus.plexus.util.FileUtils;

/**
 * Goal which prepares a Help module in compressed (jar) format for repackaging into a
 * CareWeb-compliant help module.
 */
@Mojo(name = "prepare")
@Execute(goal = "prepare", phase = LifecyclePhase.PROCESS_SOURCES)
public class HelpConverterMojo extends BaseMojo {
    
    /**
     * Base folder.
     */
    @Parameter(property = "basedir/src/main/help/", required = true)
    private String baseDirectory;
    
    /**
     * Module id
     */
    @Parameter(property = "maven.carewebframework.help.moduleId")
    private String moduleId;
    
    /**
     * Module name
     */
    @Parameter(property = "maven.carewebframework.help.moduleName")
    private String moduleName;
    
    /**
     * Help set format specifier.
     */
    @Parameter(property = "maven.carewebframework.help.format")
    private String format;
    
    /**
     * Module version
     */
    @Parameter(property = "project.version", required = true)
    private String moduleVersion;
    
    /**
     * Source file name (Help jar file)
     */
    @Parameter(property = "maven.carewebframework.help.moduleSource")
    private String moduleSource;
    
    /**
     * If true, failure to specify an existing help source jar will not result in build failure.
     */
    @Parameter(property = "maven.carewebframework.help.ignoreMissingSource", defaultValue = "false", required = false)
    private boolean ignoreMissingSource;
    
    // This is the relative path to the help set definition file.
    private String hsFilePath;
    
    /**
     * Main execution entry point for plug-in.
     */
    @Override
    public void execute() throws MojoExecutionException {
        
        if (StringUtils.isEmpty(moduleSource) && ignoreMissingSource) {
            return;
        }
        
        JarFile sourceJar = null;
        String rootPath = "org/carewebframework/help/" + moduleId + "/";
        String fullPath = "web/" + rootPath;
        
        try {
            String sourceFilename = FileUtils.normalize(baseDirectory + "/" + moduleSource);
            getLog().info("Extracting help module source from " + sourceFilename);
            try {
                sourceJar = new JarFile(sourceFilename);
            } catch (ZipException e) {
                if (ignoreMissingSource) {
                    getLog().info("Ignoring help module source ZipException");
                    return;
                }
                throw e;
            }
            File outputDirectory = newSubdirectory(buildDirectory, "help-module");
            File rootDirectory = newSubdirectory(outputDirectory, fullPath);
            Enumeration<JarEntry> entries = sourceJar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith("META-INF/")) {
                    continue;
                }
                
                if (hsFilePath == null && !entry.isDirectory() && entryName.toLowerCase().endsWith(".hs")) {
                    hsFilePath = rootPath + entryName;
                }
                
                if (entry.isDirectory()) {
                    newSubdirectory(rootDirectory, entry.getName());
                } else {
                    InputStream in = sourceJar.getInputStream(entry);
                    File outputFile = new File(rootDirectory, entry.getName());
                    FileOutputStream out = new FileOutputStream(outputFile);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
            createHelpConfigEntry(outputDirectory);
            createArchive(outputDirectory, "help");
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error.", e);
        } finally {
            try {
                if (sourceJar != null) {
                    sourceJar.close();
                }
            } catch (IOException e) {
                getLog().error("Error closing source file.", e);
            }
        }
    }
    
    /**
     * Create the xml configuration descriptor.
     * 
     * @param outputDirectory The directory where the configuration descriptor is to be created.
     * @throws MojoExecutionException
     */
    private void createHelpConfigEntry(File outputDirectory) throws MojoExecutionException {
        if (hsFilePath == null) {
            throw new MojoExecutionException("Help set definition file not found in source jar.");
        }
        
        createConfigEntry(outputDirectory, "/help-config.xml", moduleId, moduleId, hsFilePath, moduleName,
            getVersion(moduleVersion), format);
    }
    
}
