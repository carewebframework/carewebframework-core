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
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.processor.AbstractProcessor;

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
 * </pre>
 */
@Mojo(name = "prepare", requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(goal = "prepare", phase = LifecyclePhase.PREPARE_PACKAGE)
public class ThemeGeneratorMojo extends BaseMojo {
    
    
    /**
     * Themes to be built.
     */
    @Parameter(property = "themes", required = true)
    private List<Theme> themes;
    
    /**
     * Directory containing source to consider when generating themes.
     */
    @Parameter(property = "maven.careweb.theme.sourceDirectory", defaultValue = "${project.build.directory}/theme-source", required = true)
    private File sourceDirectory;
    
    /**
     * By default, all resolved dependencies will be considered in theme source. This parameter will
     * allow an explicit list. Values required as in the format of an Artifact.dependencyConflictId
     * (i.e. groupId:artifactId:type:classifier)
     */
    @Parameter(property = "themeSources", required = false)
    private List<String> themeSources;
    
    /**
     * Theme base path
     */
    @Parameter(property = "themeBase", defaultValue = "org/carewebframework/themes/${buildNumber}/", required = true)
    private String themeBase;
    
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
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping theme generation.");
            return;
        }
        
        String task = null;
        
        try {
            task = "initializing";
            init("theme", themeBase);
            task = "copying theme dependencies";
            copyDependencies();
            task = "processing themes";
            processThemes();
            task = "assembling the archive";
            assembleArchive();
            task = "cleaning up";
            FileUtils.deleteDirectory(sourceDirectory);
        } catch (Exception e) {
            throwMojoException("Exception occurred while " + task + ".", e);
        }
        
    }
    
    protected String getThemeBase() {
        return themeBase;
    }
    
    protected File getSourceDirectory() {
        return sourceDirectory;
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
        
        Set<?> deps = (excludeTransitiveDependencies ? mavenProject.getDependencyArtifacts() : mavenProject.getArtifacts());
        
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
                if (!hasSource) {
                    hasSource = true;
                    getLog().info("Copying theme dependencies.");
                }
                
                File artifactFile = a.getFile();
                File artifactCopyLocation = new File(this.sourceDirectory, artifactFile.getName());
                getLog().debug("Copying dependency : " + artifactFile);
                FileUtils.copyFile(artifactFile, artifactCopyLocation);
            }
        }
        
        return hasSource;
    }
    
    /**
     * Process all themes
     * 
     * @throws Exception unspecified exception.
     */
    private void processThemes() throws Exception {
        for (Theme theme : themes) {
            if (theme.getThemeVersion() == null) {
                theme.setThemeVersion(getModuleVersion());
            }
            
            processTheme(theme);
        }
    }
    
    /**
     * Processes a theme.
     * 
     * @param theme The theme.
     * @throws Exception Unspecified exception.
     */
    private void processTheme(Theme theme) throws Exception {
        AbstractProcessor<?> processor;
        getLog().info("Processing theme: " + theme);
        
        if (theme.getBaseColor() != null) {
            processor = new ZKThemeProcessor(theme, this);
        } else {
            processor = new CSSThemeProcessor(theme, this);
        }
        
        processor.transform();
    }
    
}
