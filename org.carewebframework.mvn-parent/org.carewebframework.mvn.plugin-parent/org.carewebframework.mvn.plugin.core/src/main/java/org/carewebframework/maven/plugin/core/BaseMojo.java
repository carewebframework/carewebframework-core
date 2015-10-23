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
import java.io.FileFilter;
import java.util.Date;
import java.util.List;

import com.google.common.io.Files;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.carewebframework.maven.plugin.processor.ResourceProcessor;

import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;

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
    
    @Parameter(property = "plugin", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;
    
    @Parameter(property = "project.build.directory", required = true)
    protected File buildDirectory;
    
    @Parameter(property = "project.version", required = true)
    protected String projectVersion;
    
    @Parameter(property = "buildNumber", required = false)
    protected String buildNumber;
    
    @Parameter(property = "noclassifier", required = false)
    protected boolean noclassifier;
    
    @Parameter(property = "archive", alias = "archive", required = false)
    protected MavenArchiveConfiguration archiveConfig = new MavenArchiveConfiguration();
    
    /**
     * Excluded files.
     */
    @Parameter(property = "exclusions", required = false)
    private List<String> exclusions;
    
    /**
     * Additional resources to copy.
     */
    @Parameter(property = "resources", required = false)
    private List<String> resources;
    
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
    @Parameter(property = "maven.carewebframework.mojo.warInclusion", defaultValue = "true", required = true)
    private boolean warInclusion;
    
    /**
     * Whether to fail build on error
     */
    @Parameter(property = "maven.carewebframework.mojo.failOnError", defaultValue = "true", required = false)
    private boolean failOnError;
    
    private FileFilter exclusionFilter;
    
    protected File stagingDirectory;
    
    protected ConfigTemplate configTemplate;
    
    protected String classifier;
    
    protected String moduleBase;
    
    /**
     * Subclasses must call this method early in their execute method.
     * 
     * @param classifier The output jar classifier.
     * @param moduleBase The base path for generated resources.
     * @throws MojoExecutionException Unspecified exception.
     */
    protected void init(String classifier, String moduleBase) throws MojoExecutionException {
        this.classifier = classifier;
        this.moduleBase = moduleBase;
        stagingDirectory = new File(buildDirectory, classifier + "-staging");
        configTemplate = new ConfigTemplate(classifier + "-spring.xml");
        exclusionFilter = exclusions == null || exclusions.isEmpty() ? null : new WildcardFileFilter(exclusions);
        archiveConfig.setAddMavenDescriptor(false);
    }
    
    public MavenProject getMavenProject() {
        return mavenProject;
    }
    
    public List<String> getResources() {
        return resources;
    }
    
    public File getBuildDirectory() {
        return buildDirectory;
    }
    
    /**
     * Form a version string from the project version and build number.
     * 
     * @return Version string.
     */
    protected String getModuleVersion() {
        StringBuilder sb = new StringBuilder();
        int pcs = 0;
        
        for (String pc : projectVersion.split("\\.")) {
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
     * Helper method to add a configuration file entry.
     * 
     * @param placeholder The insertion placeholder.
     * @param params Parameter list.
     */
    public void addConfigEntry(String placeholder, String... params) {
        configTemplate.addEntry(placeholder, params);
    }
    
    /**
     * Append the version piece to the version # under construction.
     * 
     * @param sb String builder receiving the version under construction.
     * @param pc The version piece to add. If null, it is ignored. If non-numeric, a value of "0" is
     *            used.
     */
    private void appendVersionPiece(StringBuilder sb, String pc) {
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
    public File newStagingFile(String entryName, long modTime) {
        File file = new File(stagingDirectory, entryName);
        
        if (modTime != 0) {
            file.setLastModified(modTime);
        }
        
        file.getParentFile().mkdirs();
        return file;
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param fileName Name of file to check.
     * @return True if the file is to be excluded.
     */
    public boolean isExcluded(String fileName) {
        return isExcluded(new File(fileName));
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param file File instance to check.
     * @return True if the file is to be excluded.
     */
    public boolean isExcluded(File file) {
        return exclusionFilter != null && exclusionFilter.accept(file);
    }
    
    /**
     * If "failOnError" is enabled, throws a MojoExecutionException. Otherwise, logs the exception
     * and resumes execution.
     * 
     * @param msg The exception message.
     * @param e The original exceptions.
     * @throws MojoExecutionException Thrown exception.
     */
    public void throwMojoException(String msg, Throwable e) throws MojoExecutionException {
        if (failOnError) {
            throw new MojoExecutionException(msg, e);
        } else {
            getLog().error(msg, e);
        }
    }
    
    /**
     * Assembles the archive file. Optionally, copies to the war application directory if the
     * packaging type is "war".
     * 
     * @throws Exception Unspecified exception.
     */
    protected void assembleArchive() throws Exception {
        getLog().info("Assembling " + classifier + " archive");
        
        if (resources != null && !resources.isEmpty()) {
            getLog().info("Copying additional resources.");
            new ResourceProcessor(this, moduleBase, resources).transform();
        }
        
        if (configTemplate != null) {
            getLog().info("Creating config file.");
            configTemplate.addEntry("info", pluginDescriptor.getName(), pluginDescriptor.getVersion(),
                new Date().toString());
            configTemplate.createFile(stagingDirectory);
        }
        
        try {
            File archive = createArchive();
            
            if ("war".equalsIgnoreCase(mavenProject.getPackaging()) && this.warInclusion) {
                webappLibDirectory.mkdirs();
                File webappLibArchive = new File(this.webappLibDirectory, archive.getName());
                Files.copy(archive, webappLibArchive);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred assembling archive.", e);
        }
        
    }
    
    /**
     * Creates the archive from data in the staging directory.
     * 
     * @return The archive file.
     * @throws Exception Unspecified exception.
     */
    private File createArchive() throws Exception {
        getLog().info("Creating archive.");
        Artifact artifact = mavenProject.getArtifact();
        String clsfr = noclassifier ? "" : ("-" + classifier);
        String archiveName = artifact.getArtifactId() + "-" + artifact.getVersion() + clsfr + ".jar";
        File jarFile = new File(mavenProject.getBuild().getDirectory(), archiveName);
        MavenArchiver archiver = new MavenArchiver();
        jarArchiver.addDirectory(stagingDirectory);
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);
        archiver.createArchive(mavenSession, mavenProject, archiveConfig);
        projectHelper.attachArtifact(mavenProject, jarFile, classifier);
        FileUtils.deleteDirectory(stagingDirectory);
        return jarFile;
    }
    
}
