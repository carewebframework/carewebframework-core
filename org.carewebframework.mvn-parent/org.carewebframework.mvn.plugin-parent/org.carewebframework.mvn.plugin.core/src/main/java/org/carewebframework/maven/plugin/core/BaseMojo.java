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

import java.io.File;

import com.google.common.io.Files;

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
     * Webapp lib directory.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}/WEB-INF/lib", readonly = true)
    private File webappLibDirectory;
    
    /**
     * If packaging is of type war, archives will be copied to destination dir:
     * ${project.build.directory}/${project.build.finalName}/WEB-INF/lib
     * <p>
     * <b>Note: phase must remain as prepare-package</b>
     * </p>
     */
    @Parameter(property = "maven.careweb.theme.warInclusion", defaultValue = "true", required = true)
    private boolean warInclusion;
    
    /**
     * Whether to fail build on error
     */
    @Parameter(property = "maven.careweb.theme.failOnError", defaultValue = "true", required = false)
    private boolean failOnError;
    
    protected File stagingDirectory;
    
    protected ConfigTemplate configTemplate;
    
    protected String classifier;
    
    protected String moduleVersion;
    
    /**
     * Creates a new subdirectory under the specified parent directory.
     * 
     * @param parentDirectory The directory under which the subdirectory will be created.
     * @param path The full path of the subdirectory.
     * @return The subdirectory just created.
     */
    protected static File newSubdirectory(File parentDirectory, String path) {
        File dir = new File(parentDirectory, path);
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return dir;
    }
    
    protected void init(String classifier, String version) throws MojoExecutionException {
        this.classifier = classifier;
        stagingDirectory = new File(buildDirectory, classifier + "-staging");
        configTemplate = new ConfigTemplate(classifier + "-spring.xml");
        moduleVersion = getVersion(version);
    }
    
    /**
     * Form a version string from the module version and build number.
     * 
     * @param moduleVersion The module version.
     * @return Version string.
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
    
    public void addConfigEntry(String insertionTag, String... params) {
        configTemplate.addEntry(insertionTag, params);
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
     * Creates a new file in the staging directory. Ensures that all folders in the path are also
     * created.
     * 
     * @param entryName Entry name to create.
     * @param modTime Modification timestamp for the new entry. If 0, defaults to the current time.
     * @return the new file
     */
    public File newFile(String entryName, long modTime) {
        File file = new File(stagingDirectory, entryName);
        
        if (modTime != 0) {
            file.setLastModified(modTime);
        }
        
        file.getParentFile().mkdirs();
        return file;
    }
    
    protected void assembleArchive() throws Exception {
        getLog().info("Assembling " + classifier + " archive");
        
        try {
            File archive = createArchive();
            
            if ("war".equalsIgnoreCase(mavenProject.getPackaging()) && this.warInclusion) {
                webappLibDirectory.mkdirs();
                File webappLibArchive = new File(this.webappLibDirectory, archive.getName());
                Files.copy(archive, webappLibArchive);
            }
        } catch (final Exception e) {
            throw new Exception("Exception occurred assembling theme archive.", e);
        }
        
    }
    
    public void throwMojoException(String msg, Throwable e) throws MojoExecutionException {
        if (failOnError) {
            if (e == null) {
                throw new MojoExecutionException(msg);
            }
            
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            }
            
            throw new MojoExecutionException(msg, e);
        } else {
            getLog().error(msg, e);
        }
    }
    
    /**
     * Creates the archive.
     * 
     * @return The archive file.
     * @throws Exception Unspecified exception.
     */
    private File createArchive() throws Exception {
        if (configTemplate != null) {
            getLog().info("Creating config file.");
            configTemplate.createFile(stagingDirectory);
        }
        getLog().info("Creating archive.");
        Artifact artifact = mavenProject.getArtifact();
        String archiveName = artifact.getArtifactId() + "-" + artifact.getVersion() + "-" + classifier + ".jar";
        File jarFile = new File(mavenProject.getBuild().getDirectory(), archiveName);
        MavenArchiver archiver = new MavenArchiver();
        jarArchiver.addDirectory(stagingDirectory);
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);
        getLog().info(archive.getManifestEntries().toString());
        archiver.createArchive(mavenSession, mavenProject, archive);
        projectHelper.attachArtifact(mavenProject, "jar", classifier, jarFile);
        return jarFile;
    }
}
