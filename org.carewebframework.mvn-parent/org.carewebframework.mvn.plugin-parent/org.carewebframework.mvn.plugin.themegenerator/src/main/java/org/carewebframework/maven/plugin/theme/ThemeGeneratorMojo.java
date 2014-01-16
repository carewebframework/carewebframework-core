/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.theme;

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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.io.Files;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.carewebframework.maven.plugin.core.BaseMojo;

/**
 * <p>
 * Goal which produces CareWeb theme modules from selected source jars.
 * </p>
 * 
 * <pre>
 * {@code
 * 
 *             <plugin>
 *                 <groupId>org.carewebframework</groupId>
 *                 <artifactId>org.carewebframework.maven.plugin.themegenerator</artifactId>
 *                 <version>3.0.0-SNAPSHOT</version>
 *                 <configuration>
 *                     <themes>
 *                         <theme>
 *                             <themeName>green</themeName>
 *                             <baseColor>003300</baseColor>
 *                         </theme>
 *                         <theme>
 *                             <themeName>lilac</themeName>
 *                             <baseColor>3e48ac</baseColor>
 *                         </theme>
 *                     </themes>
 *                 </configuration>
 *                 <executions>
 *                     <execution>
 *                         <goals>
 *                             <goal>prepare</goal>
 *                         </goals>
 *                     </execution>
 *                 </executions>
 *             </plugin>
 * }
 * </pre>
 * <p>
 * In most cases, you will want your build to only consider certain artifacts and not all resolved
 * dependencies. If this is the case consider adding the following:
 * </p>
 * 
 * <pre>
 * {@code
 *                     <themeSources>
 *                        <themeSource>org.zkoss.zk:zk:jar</themeSource>
 *                        <themeSource>org.zkoss.zkforge.el:zcommons-el:jar</themeSource>
 *                        <themeSource>org.zkoss.zk:zkex:jar</themeSource>
 *                        <themeSource>org.zkoss.zk:zkmax:jar</themeSource>
 *                        <themeSource>org.zkoss.zk:zul:jar</themeSource>
 *                        <themeSource>org.zkoss.common:zweb:jar</themeSource>
 *                     </themeSources>
 * 
 * }
 */
@Mojo(name = "prepare", requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(goal = "prepare", phase = LifecyclePhase.PREPARE_PACKAGE)
public class ThemeGeneratorMojo extends BaseMojo {
    
    private static final String ARCHIVE_PREFIX = "theme-";
    
    /**
     * Work directory containing theme generation results. Note: archives will be created in
     * ${project.build.directory}.
     */
    @Parameter(property = "maven.careweb.theme.buildDirectory", defaultValue = "${project.build.directory}/theme-module", required = true)
    private File buildDirectory;
    
    /**
     * Directory containing source to consider when generating themes.
     */
    @Parameter(property = "maven.careweb.theme.sourceDirectory", defaultValue = "${project.build.directory}/theme-source", required = true)
    private File sourceDirectory;
    
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
     * Exclude files.
     */
    @Parameter(property = "maven.careweb.theme.exclusions", required = true)
    private List<String> exclusions;
    
    /**
     * Themes to be built.
     */
    @Parameter(property = "themes", required = true)
    private List<Theme> themes;
    
    /**
     * By default, all resolved dependencies will be considered in theme source. This parameter will
     * allow an explicit list. Values required as in the format of an Artifact.dependencyConflictId
     * (i.e. groupId:artifactId:type:classifier)
     */
    @Parameter(property = "themeSources", required = false)
    private List<String> themeSources;
    
    /**
     * Theme version
     */
    @Parameter(property = "maven.careweb.theme.version", defaultValue = "${project.version}", required = true)
    private String themeVersion;
    
    /**
     * Whether to fail build on error
     */
    @Parameter(property = "maven.careweb.theme.failOnError", defaultValue = "true", required = false)
    private boolean failOnError;
    
    /**
     * Whether to exclude transitive dependencies when considering theme source. Default is false.
     * In other words, all resolved dependencies are considered unless
     * <code>excludeTransitiveDependencies</code> is true, in which case only direct dependencies
     * are considered.
     */
    @Parameter(property = "maven.careweb.theme.excludeTransitiveDependencies", defaultValue = "false", required = false)
    private boolean excludeTransitiveDependencies;
    
    /**
     * Whether to skip processing
     */
    @Parameter(property = "maven.careweb.theme.skip", defaultValue = "false", required = false)
    private boolean skip;
    
    private List<ThemeGenerator> themeGenerators;
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (this.skip) {
            getLog().info("Skipping theme generation.");
            return;
        }
        
        try {
            getLog().info("Preparing theme source.");
            prepareSource();
            copyDependencies();
        } catch (final Exception e) {
            throwMojoException("Exception occurred preparing theme source.", e);
        }
        
        try {
            getLog().info("Initializing theme generators.");
            initThemeGenerators();
        } catch (final Exception e) {
            throwMojoException("Exception occurred initializing theme generators.", e);
        }
        
        try {
            getLog().info("Processing theme sources.");
            processSources();
        } catch (final Exception e) {
            throwMojoException("Exception occurred processing source files for theme(s).", e);
        }
        
        try {
            createThemeConfigEntryAndAssembleArchive();
        } catch (final Exception e) {
            throwMojoException("Exception occurred creating theme config and assembly", e);
        }
    }
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    private void prepareSource() throws Exception {
        this.sourceDirectory.mkdir();
        if (!this.sourceDirectory.exists() || !this.sourceDirectory.isDirectory()) {
            throw new Exception("Source directory was not found: " + this.sourceDirectory);
        }
    }
    
    private void copyDependencies() throws Exception {
        boolean copyExplicitThemeSourceArtifacts = false;
        boolean hasNoSource = true;
        
        if (this.themeSources != null) {
            getLog().debug("Theme-sources list: " + this.themeSources);
            copyExplicitThemeSourceArtifacts = true;
            getLog().info("Default theme source based on dependencies overridden by configuration");
        }
        
        final Set<?> deps = (this.excludeTransitiveDependencies ? this.mavenProject.getDependencyArtifacts()
                : this.mavenProject.getArtifacts());
        
        for (final Object o : deps) {
            boolean copyDependency = true;
            final Artifact a = (Artifact) o;
            getLog().debug("Artifact: " + a);
            
            if (copyExplicitThemeSourceArtifacts) {
                final String dependencyConflictId = a.getDependencyConflictId();
                
                if (!this.themeSources.contains(dependencyConflictId)) {
                    getLog().debug("Ignoring dependency from theme source: " + dependencyConflictId);
                    copyDependency = false;
                }
            }
            if (copyDependency) {
                hasNoSource = false;
                final File artifactFile = a.getFile();
                final File artifactCopyLocation = new File(this.sourceDirectory, artifactFile.getName());
                getLog().debug("Copying dependency : " + artifactFile);
                Files.copy(artifactFile, artifactCopyLocation);
            }
        }
        if (hasNoSource) {
            throw new Exception("There is no theme source to consider");
        }
    }
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    private void initThemeGenerators() throws Exception {
        this.themeGenerators = new ArrayList<ThemeGenerator>();
        
        for (final Theme theme : this.themes) {
            this.themeGenerators.add(new ThemeGenerator(theme.getThemeName(), theme.getBaseColor(),
                    getVersion(themeVersion), buildDirectory, new WildcardFileFilter(this.exclusions)));
            getLog().info("Considering the following theme for processing: " + theme);
        }
    }
    
    /**
     * Process all jars in the source folder.
     * 
     * @throws Exception
     */
    private void processSources() throws Exception {
        final FileFilter filter = new WildcardFileFilter("*.jar");
        
        for (final File file : this.sourceDirectory.listFiles(filter)) {
            try {
                processJarFile(file);
            } catch (final Exception e) {
                throw new Exception("Exception occurred processing source jar:" + file.getName(), e);
            }
        }
    }
    
    /**
     * Process one jar file from the source folder.
     * 
     * @param file A jar file from the source folder.
     * @throws Exception
     */
    private void processJarFile(final File file) throws Exception {
        final JarFile sourceJar = new JarFile(file);
        final Enumeration<JarEntry> entries = sourceJar.entries();
        boolean wasProcessed = false;
        
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            
            if (entry.isDirectory()) {
                continue;
            }
            
            for (final ThemeGenerator themeGen : this.themeGenerators) {
                if (!themeGen.process(sourceJar, entry)) {
                    break;
                } else {
                    wasProcessed = true;
                }
            }
            
        }
        sourceJar.close();
        
        if (!wasProcessed) {
            getLog().info("Source jar contained no themeable resources: " + file.getName());
        }
    }
    
    private void createThemeConfigEntryAndAssembleArchive() throws Exception {
        for (final ThemeGenerator gen : this.themeGenerators) {
            getLog().info("Creating theme config for theme: " + gen.getThemeName());
            createConfigEntry(gen.getBuildDirectory(), "/theme-config.xml", ARCHIVE_PREFIX + gen.getThemeName(),
                gen.getThemeName(), getVersion(themeVersion));
        }
        
        getLog().info("Assembling theme archive");
        
        try {
            File archive = createArchive(buildDirectory, "theme");
            
            if ("war".equalsIgnoreCase(mavenProject.getPackaging()) && this.warInclusion) {
                webappLibDirectory.mkdirs();
                File webappLibArchive = new File(this.webappLibDirectory, archive.getName());
                Files.copy(archive, webappLibArchive);
            }
        } catch (final Exception e) {
            throw new Exception("Exception occurred assembling theme archive.", e);
        }
        
    }
    
    private void throwMojoException(final String msg, final Throwable e) throws MojoExecutionException {
        if (this.failOnError) {
            throw new MojoExecutionException(msg, e);
        } else {
            getLog().error(msg, e);
        }
    }
}
