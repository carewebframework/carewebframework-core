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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchive;
import org.carewebframework.maven.plugin.help.SourceLoader.ISourceArchiveEntry;

import org.codehaus.plexus.util.FileUtils;

/**
 * Goal which prepares a Help module in native format for repackaging into a CareWeb-compliant help
 * module.
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
    @Parameter(property = "maven.carewebframework.help.moduleFormat")
    private String moduleFormat;
    
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
    
    /**
     * Additional archive loader classes.
     */
    @Parameter(property = "maven.carewebframework.help.archiveLoaders")
    private List<SourceLoader> archiveLoaders;
    
    // This is the relative path to the help set definition file.
    private String hsFilePath;
    
    // Maps help format specifier to the associated source archive loader.
    private final Map<String, SourceLoader> sourceArchiveMap = new HashMap<String, SourceLoader>();
    
    /**
     * Main execution entry point for plug-in.
     */
    @Override
    public void execute() throws MojoExecutionException {
        
        if (StringUtils.isEmpty(moduleSource) && ignoreMissingSource) {
            return;
        }
        
        init("help", moduleVersion);
        registerLoader(new SourceLoader("javahelp", "*.hs", ZipSource.class.getName()));
        registerLoader(new SourceLoader("ohj", "*.hs", ZipSource.class.getName()));
        registerExternalLoaders();
        
        SourceLoader loader = sourceArchiveMap.get(moduleFormat);
        
        if (loader == null) {
            throw new MojoExecutionException("No source loader found for format " + moduleFormat);
        }
        
        ISourceArchive sourceArchive = null;
        String rootPath = "org/carewebframework/help/content/" + moduleId + "/";
        String fullPath = "web/" + rootPath;
        
        try {
            String sourceFilename = FileUtils.normalize(baseDirectory + "/" + moduleSource);
            getLog().info("Extracting help module source from " + sourceFilename);
            sourceArchive = loader.load(sourceFilename);
            File outputDirectory = newSubdirectory(buildDirectory, "help-module");
            File rootDirectory = newSubdirectory(outputDirectory, fullPath);
            Iterator<? extends ISourceArchiveEntry> entries = sourceArchive.entries();
            
            while (entries.hasNext()) {
                ISourceArchiveEntry entry = entries.next();
                String entryPath = entry.getRelativePath();
                
                if (entryPath.startsWith("META-INF/")) {
                    continue;
                }
                
                if (hsFilePath == null && loader.isHelpSetFile(entryPath)) {
                    hsFilePath = rootPath + entryPath;
                }
                
                if (entry.isDirectory()) {
                    newSubdirectory(rootDirectory, entryPath);
                } else {
                    InputStream in = entry.getInputStream();
                    File outputFile = new File(rootDirectory, entryPath);
                    FileOutputStream out = new FileOutputStream(outputFile);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
            
            if (hsFilePath == null) {
                throwMojoException("Help set definition file not found in source jar.", null);
            }
            
            addConfigEntry("help", moduleId, moduleId, hsFilePath, moduleName, getVersion(moduleVersion), moduleFormat);
            assembleArchive();
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error.", e);
        } finally {
            if (sourceArchive != null) {
                sourceArchive.close();
            }
        }
    }
    
    /**
     * Adds any additional source loaders specified in configuration.
     */
    private void registerExternalLoaders() {
        if (archiveLoaders != null) {
            for (SourceLoader loader : archiveLoaders) {
                registerLoader(loader);
            }
        }
    }
    
    private void registerLoader(SourceLoader loader) {
        sourceArchiveMap.put(loader.getFormatSpecifier(), loader);
    }
    
}
