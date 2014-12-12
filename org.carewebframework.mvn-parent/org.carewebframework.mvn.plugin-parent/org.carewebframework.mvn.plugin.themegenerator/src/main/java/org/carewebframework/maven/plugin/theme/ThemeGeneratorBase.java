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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

/**
 * Generates a new theme from a base theme by using specialized processors to transform individual
 * theme elements.
 */
public abstract class ThemeGeneratorBase {
    
    /**
     * Abstract base class for processing jar file entries. Override the abstract process method to
     * implement the logic for processing a jar entry.
     */
    protected abstract class ResourceProcessor {
        
        protected InputStream inputStream;
        
        protected OutputStream outputStream;
        
        protected void process(IThemeResource resource) throws Exception {
            this.inputStream = resource.getInputStream();
            this.outputStream = new FileOutputStream(newFile(resource));
            process();
            this.inputStream.close();
            this.outputStream.close();
        }
        
        protected abstract void process() throws Exception;
    }
    
    /**
     * Performs a simple copy of a resource from the source to the destination.
     */
    protected class CopyProcessor extends ResourceProcessor {
        
        @Override
        protected void process() throws Exception {
            IOUtils.copy(this.inputStream, this.outputStream);
        }
    }
    
    private static final String THEME_NAME_REGEX = "^[\\w\\-]+$";
    
    private static final Pattern URL_PATTERN = Pattern.compile("~\\./");
    
    private final WildcardFileFilter exclusionFilters;
    
    protected final Log log;
    
    private final String rootPath;
    
    private final Theme theme;
    
    private final File buildDirectory;
    
    private final Map<String, ResourceProcessor> processors = new HashMap<String, ResourceProcessor>();
    
    /**
     * @param theme The theme.
     * @param buildDirectory Scratch build directory
     * @param exclusionFilters WildcardFileFilter (i.e. exclude certain files)
     * @param log The logger
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorBase(Theme theme, File buildDirectory, WildcardFileFilter exclusionFilters, Log log)
        throws Exception {
        if (theme == null || theme.getThemeName() == null || !theme.getThemeName().matches(THEME_NAME_REGEX)) {
            throw new Exception(
                    "Theme names must not be null and must be alphanumeric with no blanks, conforming to regexp: "
                            + THEME_NAME_REGEX);
        }
        this.exclusionFilters = exclusionFilters;
        this.theme = theme;
        this.buildDirectory = buildDirectory;
        this.rootPath = getRootPath() + theme.getThemeName();
        this.log = log;
        registerProcessors(processors);
    }
    
    protected abstract void registerProcessors(Map<String, ResourceProcessor> processors);
    
    protected abstract String getConfigTemplate();
    
    protected abstract String getRootPath();
    
    protected abstract String relocateResource(String resourceName, String rootPath);
    
    /**
     * Adjust any url references in the line to use new root path.
     * 
     * @param line String to modify
     * @return the modified string
     */
    public String replaceURLs(String line) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = URL_PATTERN.matcher(line);
        String newPath = "~./" + rootPath + "/";
        
        while (matcher.find()) {
            char dlm = line.charAt(matcher.start() - 1);
            int i = line.indexOf(dlm, matcher.end());
            String url = i > 0 ? line.substring(matcher.start(), i) : null;
            
            if (url == null || (!isExcluded(url) && getProcessor(url) != null)) {
                matcher.appendReplacement(sb, newPath);
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Creates a new file in the build directory. Ensures that all folders in the path are also
     * created.
     * 
     * @param entryName Entry name to create.
     * @param modTime Modification timestamp for the new entry. If 0, defaults to the current time.
     * @return the new file
     */
    public File newFile(String entryName, long modTime) {
        File file = new File(this.buildDirectory, entryName);
        
        if (modTime != 0) {
            file.setLastModified(modTime);
        }
        
        file.getParentFile().mkdirs();
        return file;
    }
    
    /**
     * Creates a new jar file entry from an existing theme resource.
     * 
     * @param resource Old resource.
     * @return The new file
     */
    public File newFile(IThemeResource resource) {
        return newFile(relocateResource(resource.getName(), rootPath), resource.getTime());
    }
    
    /**
     * Finds and executes the processor appropriate for the theme resource.
     * 
     * @param resource The theme resource.
     * @return True if a processor was found for the jar entry.
     * @throws Exception Unspecified exception.
     */
    public boolean process(IThemeResource resource) throws Exception {
        final String name = StringUtils.trimToEmpty(resource.getName());
        
        if (isExcluded(name)) {
            return false;
        }
        
        ResourceProcessor processor = getProcessor(name);
        
        if (processor != null) {
            processor.process(resource);
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the processor for the file, or null if none registered.
     * 
     * @param fileName The file name.
     * @return The associated processor, or null if not found.
     */
    private ResourceProcessor getProcessor(String fileName) {
        final String fileLower = fileName.toLowerCase();
        
        for (Entry<String, ResourceProcessor> entry : processors.entrySet()) {
            if (fileLower.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param fileName Name of file to check.
     * @return True if the file is to be excluded.
     */
    protected boolean isExcluded(String fileName) {
        return isExcluded(new File(fileName));
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param file File instance to check.
     * @return True if the file is to be excluded.
     */
    protected boolean isExcluded(File file) {
        return exclusionFilters.accept(file);
    }
    
    /**
     * @return The theme.
     */
    public Theme getTheme() {
        return theme;
    }
    
    /**
     * @return the buildDirectory
     */
    public File getBuildDirectory() {
        return buildDirectory;
    }
    
}
