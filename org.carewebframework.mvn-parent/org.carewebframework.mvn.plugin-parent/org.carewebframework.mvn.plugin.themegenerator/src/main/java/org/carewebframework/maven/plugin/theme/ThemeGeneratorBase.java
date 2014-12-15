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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

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
    
    private final Theme theme;
    
    protected final ThemeGeneratorMojo mojo;
    
    private final Map<String, ResourceProcessor> processors = new HashMap<String, ResourceProcessor>();
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorBase(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        
        if (theme != null) {
            if (theme.getThemeName() == null || !theme.getThemeName().matches(THEME_NAME_REGEX)) {
                throw new Exception("Theme names must be alphanumeric with no blanks, conforming to regexp: "
                        + THEME_NAME_REGEX);
            }
        }
        this.theme = theme;
        this.mojo = mojo;
        registerProcessors(processors);
    }
    
    protected abstract void registerProcessors(Map<String, ResourceProcessor> processors);
    
    protected abstract String relocateResource(String resourceName);
    
    protected abstract void process() throws Exception;
    
    protected abstract String getResourceBase();
    
    /**
     * Adjust any url references in the line to use new root path.
     * 
     * @param line String to modify
     * @return the modified string
     */
    public String replaceURLs(String line) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = URL_PATTERN.matcher(line);
        String newPath = "~./" + getResourceBase() + "/";
        
        while (matcher.find()) {
            char dlm = line.charAt(matcher.start() - 1);
            int i = line.indexOf(dlm, matcher.end());
            String url = i > 0 ? line.substring(matcher.start(), i) : null;
            
            if (url == null || (!mojo.isExcluded(url) && getProcessor(url) != null)) {
                matcher.appendReplacement(sb, newPath);
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Creates a new jar file entry from an existing theme resource.
     * 
     * @param resource Old resource.
     * @return The new file
     */
    public File newFile(IThemeResource resource) {
        return mojo.newFile(relocateResource(resource.getName()), resource.getTime());
    }
    
    protected void addConfigEntry(String insertionTag, String... params) {
        params = (String[]) ArrayUtils.addAll(
            new String[] { theme.getThemeName(), theme.getThemeVersion(), mojo.getThemeBase() }, params);
        mojo.addConfigEntry(insertionTag, params);
    }
    
    /**
     * Finds and executes the processor appropriate for the theme resource.
     * 
     * @param resource The theme resource.
     * @return True if a processor was found for the jar entry.
     * @throws Exception Unspecified exception.
     */
    protected boolean process(IThemeResource resource) throws Exception {
        final String name = StringUtils.trimToEmpty(resource.getName());
        
        if (mojo.isExcluded(name)) {
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
     * @return The theme.
     */
    public Theme getTheme() {
        return theme;
    }
    
}
