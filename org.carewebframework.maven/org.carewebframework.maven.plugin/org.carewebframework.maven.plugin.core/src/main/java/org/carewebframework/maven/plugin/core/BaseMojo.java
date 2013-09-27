/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.core;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Base plugin.
 */
public abstract class BaseMojo extends AbstractMojo {
    
    @Component(role = org.codehaus.plexus.archiver.Archiver.class, hint = "jar")
    protected JarArchiver jarArchiver;
    
    @Component()
    protected MavenProjectHelper projectHelper;
    
    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject mavenProject;
    
    @Parameter(property = "session", readonly = true, required = true)
    protected MavenSession mavenSession;
    
    @Parameter(property = "project.build.directory", required = true)
    protected File buildDirectory;
    
    @Parameter(property = "buildNumber", required = false)
    protected String buildNumber;
    
    @Parameter(property = "archive", required = false)
    protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
    
    /**
     * Creates a new subdirectory under the specified parent directory.
     * 
     * @param parentDirectory The directory under which the subdirectory will be created.
     * @param path The full path of the subdirectory.
     * @return The subdirectory just created.
     */
    protected File newSubdirectory(File parentDirectory, String path) {
        File dir = new File(parentDirectory, path);
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return dir;
    }
    
    /**
     * Form a version string from the module version and build number.
     * 
     * @param moduleVersion
     * @return
     */
    protected String getVersion(String moduleVersion) {
        StringBuilder sb = new StringBuilder();
        int pcs = 0;
        
        for (String pc : moduleVersion.split("\\.")) {
            if (pcs++ > 3) {
                break;
            } else {
                appendVersionPiece(sb, pc);
            }
        }
        
        appendVersionPiece(sb, buildNumber);
        return sb.toString();
    }
    
    /**
     * Append the version piece to the version # under construction.
     * 
     * @param sb String builder receiving the version under construction.
     * @param pc The version piece to add. If null, it is ignored. If non-numeric, a value of "0" is
     *            used.
     */
    private void appendVersionPiece(final StringBuilder sb, String pc) {
        if ((pc != null) && !pc.isEmpty()) {
            pc = pc.trim();
            
            if (sb.length() > 0) {
                sb.append(".");
            }
            
            sb.append(StringUtils.isNumeric(pc) ? pc : "0");
        }
    }
    
    /**
     * Create the xml configuration descriptor.
     * 
     * @param parentDirectory The parent directory where the configuration descriptor is to be
     *            created.
     * @param template Path to config file template.
     * @param moduleId The id of the module (used to name the spring config file).
     * @param params Replaceable parameters for the template.
     * @throws MojoExecutionException
     */
    protected void createConfigEntry(File parentDirectory, String template, String moduleId, String... params)
                                                                                                              throws MojoExecutionException {
        getLog().info("Building Spring configuration descriptor.");
        InputStream in = getClass().getResourceAsStream(template);
        
        if (in == null) {
            throw new MojoExecutionException("Cannot find config file template.");
        }
        
        File targetDirectory = newSubdirectory(parentDirectory, "META-INF");
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintStream ps = null;
        
        try {
            isr = new InputStreamReader(in);
            br = new BufferedReader(isr);
            File newEntry = new File(targetDirectory, moduleId + "-spring.xml");
            FileOutputStream out = new FileOutputStream(newEntry);
            ps = new PrintStream(out);
            String line;
            
            while ((line = br.readLine()) != null) {
                if (line.contains("{")) {
                    for (int i = 0; i < params.length; i++) {
                        line = line.replace("{" + i + "}", StringUtils.defaultString(params[i]));
                    }
                }
                ps.println(line);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error while create configuration file.", e);
        } finally {
            IOUtils.closeQuietly(ps);
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(in);
        }
    }
    
    /**
     * Creates the archive.
     * 
     * @param archiveSourceDir
     * @param classifier
     * @return The archive file.
     * @throws Exception
     */
    protected File createArchive(File archiveSourceDir, String classifier) throws Exception {
        getLog().info("Creating archive.");
        Artifact artifact = mavenProject.getArtifact();
        String archiveName = artifact.getArtifactId() + "-" + artifact.getVersion() + "-" + classifier + ".jar";
        File jarFile = new File(mavenProject.getBuild().getDirectory(), archiveName);
        MavenArchiver archiver = new MavenArchiver();
        jarArchiver.addDirectory(archiveSourceDir);
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);
        getLog().info(archive.getManifestEntries().toString());
        archiver.createArchive(mavenSession, mavenProject, archive);
        projectHelper.attachArtifact(mavenProject, "jar", classifier, jarFile);
        return jarFile;
    }
}
