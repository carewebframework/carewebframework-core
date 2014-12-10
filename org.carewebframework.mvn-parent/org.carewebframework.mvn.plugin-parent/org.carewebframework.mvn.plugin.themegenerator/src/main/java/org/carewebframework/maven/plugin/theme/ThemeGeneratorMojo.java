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

import org.codehaus.plexus.util.FileUtils;

/**
 * <p>
 * Goal which produces CareWeb theme modules from selected source jars or from source stylesheets.
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
 *                              <themeName>cerulean</themeName>
 *                              <themeUri>src/main/themes/cerulean/bootstrap.min.css</themeUri>
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
    
    private List<ThemeGeneratorBase> themeGenerators;
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping theme generation.");
            return;
        }
        
        boolean wasProcessed = false;
        
        try {
            validateSource();
            wasProcessed = copyDependencies();
        } catch (final Exception e) {
            throwMojoException("Exception occurred validating theme source.", e);
        }
        
        try {
            getLog().info("Initializing theme generators.");
            initThemeGenerators();
        } catch (final Exception e) {
            throwMojoException("Exception occurred initializing theme generators.", e);
        }
        
        try {
            getLog().info("Processing theme sources.");
            
            if (wasProcessed) {
                processZKSources();
            }
            
            wasProcessed = processCSSSources() || wasProcessed;
            
            if (!wasProcessed) {
                throw new Exception("No theme resources were found for processing.");
            }
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
     * Ensure that source directory exists.
     * 
     * @throws Exception Unspecified exception.
     */
    private void validateSource() throws Exception {
        getLog().info("Validating theme source.");
        FileUtils.forceMkdir(sourceDirectory);
        
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            throw new Exception("Source directory was not found: " + sourceDirectory);
        }
    }
    
    /**
     * Copies dependencies to build directory for processing.
     * 
     * @return True if dependencies were copied.
     * @throws Exception Unspecified exception.
     */
    private boolean copyDependencies() throws Exception {
        boolean copyExplicitThemeSourceArtifacts = false;
        boolean hasSource = false;
        
        if (themeSources != null) {
            getLog().debug("Theme-sources list: " + themeSources);
            copyExplicitThemeSourceArtifacts = true;
            getLog().info("Default theme source based on dependencies overridden by configuration");
        }
        
        final Set<?> deps = (excludeTransitiveDependencies ? mavenProject.getDependencyArtifacts() : mavenProject
                .getArtifacts());
        
        for (Object o : deps) {
            boolean copyDependency = true;
            Artifact a = (Artifact) o;
            getLog().debug("Artifact: " + a);
            
            if (copyExplicitThemeSourceArtifacts) {
                String dependencyConflictId = a.getDependencyConflictId();
                
                if (!this.themeSources.contains(dependencyConflictId)) {
                    getLog().debug("Ignoring dependency from theme source: " + dependencyConflictId);
                    copyDependency = false;
                }
            }
            if (copyDependency) {
                hasSource = true;
                File artifactFile = a.getFile();
                File artifactCopyLocation = new File(this.sourceDirectory, artifactFile.getName());
                getLog().debug("Copying dependency : " + artifactFile);
                Files.copy(artifactFile, artifactCopyLocation);
            }
        }
        
        return hasSource;
    }
    
    /**
     * Generates a theme generator instance for each source theme.
     * 
     * @throws Exception Unspecified exception.
     */
    private void initThemeGenerators() throws Exception {
        themeGenerators = new ArrayList<ThemeGeneratorBase>();
        
        for (Theme theme : themes) {
            getLog().info("Considering the following theme for processing: " + theme);
            ThemeGeneratorBase themeGenerator;
            
            if (theme.getBaseColor() != null) {
                themeGenerator = new ThemeGeneratorZK(theme, buildDirectory, new WildcardFileFilter(exclusions));
            } else {
                themeGenerator = new ThemeGeneratorCSS(theme, buildDirectory, new WildcardFileFilter(exclusions));
            }
            
            themeGenerators.add(themeGenerator);
        }
    }
    
    /**
     * Process all jars in the source folder.
     * 
     * @throws Exception Unspecified exception.
     */
    private void processZKSources() throws Exception {
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
     * @throws Exception Unspecified exception.
     */
    private void processJarFile(File file) throws Exception {
        JarFile sourceJar = new JarFile(file);
        Enumeration<JarEntry> entries = sourceJar.entries();
        boolean wasProcessed = false;
        
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            
            if (entry.isDirectory()) {
                continue;
            }
            
            IThemeResource resource = new ThemeResourceJarEntry(sourceJar, entry);
            
            for (ThemeGeneratorBase gen : themeGenerators) {
                if (gen instanceof ThemeGeneratorZK) {
                    if (!gen.process(resource)) {
                        break;
                    } else {
                        wasProcessed = true;
                    }
                }
            }
            
        }
        sourceJar.close();
        
        if (!wasProcessed) {
            getLog().info("Source jar contained no themeable resources: " + file.getName());
        }
    }
    
    /**
     * Process simple CSS themes.
     * 
     * @return True if any themes were processed.
     */
    private boolean processCSSSources() throws Exception {
        boolean wasProcessed = false;
        
        for (ThemeGeneratorBase gen : themeGenerators) {
            if (gen instanceof ThemeGeneratorCSS) {
                Theme theme = gen.getTheme();
                String mapper = theme.getCSSMapper();
                File file = new File(mavenProject.getBasedir(), theme.getThemeUri());
                File map = mapper == null ? null : new File(mavenProject.getBasedir(), mapper);
                IThemeResource resource = new ThemeResourceCSS(file, map);
                wasProcessed = gen.process(resource) || wasProcessed;
            }
        }
        
        return wasProcessed;
    }
    
    private void createThemeConfigEntryAndAssembleArchive() throws Exception {
        for (ThemeGeneratorBase gen : themeGenerators) {
            Theme theme = gen.getTheme();
            String themeName = theme.getThemeName();
            String fileName = theme.getThemeUri() == null ? null : FileUtils.filename(theme.getThemeUri());
            getLog().info("Creating theme config for theme: " + themeName);
            createConfigEntry(gen.getBuildDirectory(), gen.getConfigTemplate(), ARCHIVE_PREFIX + themeName, themeName,
                getVersion(themeVersion), fileName);
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
    
    private void throwMojoException(String msg, Throwable e) throws MojoExecutionException {
        if (failOnError) {
            throw new MojoExecutionException(msg, e);
        } else {
            getLog().error(msg, e);
        }
    }
}
