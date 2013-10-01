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
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
    
    public interface ISourceArchive {
        
        Enumeration<? extends ZipEntry> entries();
        
        InputStream getInputStream(ZipEntry entry) throws IOException;
        
        boolean isHelpSetDefinition(ZipEntry entry);
        
        void close() throws IOException;
        
    }
    
    protected static class ZipFileEx extends ZipFile implements ISourceArchive {
        
        public ZipFileEx(String file) throws ZipException, IOException {
            super(file);
        }
        
        @Override
        public boolean isHelpSetDefinition(ZipEntry entry) {
            return entry.getName().endsWith(".hs");
        }
        
    }
    
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
     * Additional source archive loader classes. Format is:
     * <p>
     * format specifier = ISourceArchive implementation class
     */
    @Parameter(property = "maven.carewebframework.help.sourceLoaders")
    private List<String> sourceLoaders;
    
    // This is the relative path to the help set definition file.
    private String hsFilePath;
    
    private final Map<String, String> sourceLoaderMap = new HashMap<String, String>();
    
    /**
     * Main execution entry point for plug-in.
     */
    @Override
    public void execute() throws MojoExecutionException {
        
        if (StringUtils.isEmpty(moduleSource) && ignoreMissingSource) {
            return;
        }
        
        sourceLoaderMap.put("javahelp", ZipFileEx.class.getName());
        sourceLoaderMap.put("ohj", ZipFileEx.class.getName());
        processLoaders();
        ISourceArchive sourceArchive = null;
        String rootPath = "org/carewebframework/help/content/" + moduleId + "/";
        String fullPath = "web/" + rootPath;
        
        try {
            String sourceFilename = FileUtils.normalize(baseDirectory + "/" + moduleSource);
            getLog().info("Extracting help module source from " + sourceFilename);
            sourceArchive = getSourceArchive(sourceFilename);
            File outputDirectory = newSubdirectory(buildDirectory, "help-module");
            File rootDirectory = newSubdirectory(outputDirectory, fullPath);
            Enumeration<? extends ZipEntry> entries = sourceArchive.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith("META-INF/")) {
                    continue;
                }
                
                if (hsFilePath == null && sourceArchive.isHelpSetDefinition(entry)) {
                    hsFilePath = rootPath + entryName;
                }
                
                if (entry.isDirectory()) {
                    newSubdirectory(rootDirectory, entry.getName());
                } else {
                    InputStream in = sourceArchive.getInputStream(entry);
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
                if (sourceArchive != null) {
                    sourceArchive.close();
                }
            } catch (IOException e) {
                getLog().error("Error closing source file.", e);
            }
        }
    }
    
    /**
     * Adds any additional source loaders specified in configuration.
     */
    private void processLoaders() {
        if (sourceLoaders != null) {
            for (String sourceLoader : sourceLoaders) {
                String[] pcs = sourceLoader.split("\\=", 2);
                sourceLoaderMap.put(pcs[0], pcs[1]);
            }
        }
    }
    
    /**
     * Returns an ISourceArchive implementation for the given archive name.
     * 
     * @param archiveName Name of the archive file.
     * @return An ISourceArchive instance.
     * @throws MojoExecutionException
     */
    private ISourceArchive getSourceArchive(String archiveName) throws MojoExecutionException {
        String className = sourceLoaderMap.get(moduleFormat);
        
        if (className == null) {
            throw new MojoExecutionException("No source loader found for module format: " + moduleFormat);
        }
        
        try {
            @SuppressWarnings("unchecked")
            Class<? extends ISourceArchive> clazz = (Class<? extends ISourceArchive>) Class.forName(className);
            return clazz.getConstructor(String.class).newInstance(archiveName);
        } catch (Exception e) {
            throw new MojoExecutionException("Error processing source archive.", e);
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
            getVersion(moduleVersion), moduleFormat);
    }
    
}
