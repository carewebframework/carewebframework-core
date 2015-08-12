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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.iterator.ZipIterator;

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
     * Help module base path
     */
    @Parameter(property = "moduleBase", defaultValue = "org/carewebframework/help/content/", required = true)
    private String moduleBase;
    
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
    private List<String> archiveLoaders;
    
    // Maps help format specifier to the associated source archive loader.
    private final Map<String, SourceLoader> sourceLoaders = new HashMap<String, SourceLoader>();
    
    public String getModuleBase() {
        return moduleBase;
    }
    
    public String getModuleId() {
        return moduleId;
    }
    
    /**
     * Main execution entry point for plug-in.
     */
    @Override
    public void execute() throws MojoExecutionException {
        
        if (StringUtils.isEmpty(moduleSource) && ignoreMissingSource) {
            getLog().info("No help module source specified.");
            return;
        }
        
        init("help", moduleBase);
        registerLoader(new SourceLoader("javahelp", "*.hs", ZipIterator.class.getName()));
        registerLoader(new SourceLoader("ohj", "*.hs", ZipIterator.class.getName()));
        registerExternalLoaders();
        SourceLoader loader = sourceLoaders.get(moduleFormat);
        
        if (loader == null) {
            throw new MojoExecutionException("No source loader found for format " + moduleFormat);
        }
        
        try {
            String sourceFilename = FileUtils.normalize(baseDirectory + "/" + moduleSource);
            HelpProcessor processor = new HelpProcessor(this, sourceFilename, loader);
            processor.transform();
            addConfigEntry("help", moduleId, processor.getHelpSetFile(), moduleName, getModuleVersion(), moduleFormat);
            assembleArchive();
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error.", e);
        }
    }
    
    /**
     * Adds any additional source loaders specified in configuration.
     * 
     * @throws MojoExecutionException Error registering external loader.
     */
    private void registerExternalLoaders() throws MojoExecutionException {
        if (archiveLoaders != null) {
            for (String entry : archiveLoaders) {
                try {
                    SourceLoader loader = (SourceLoader) Class.forName(entry).newInstance();
                    registerLoader(loader);
                } catch (Exception e) {
                    throw new MojoExecutionException("Error registering archive loader for class: " + entry, e);
                }
            }
        }
    }
    
    private void registerLoader(SourceLoader loader) {
        sourceLoaders.put(loader.getFormatSpecifier(), loader);
    }
    
}
