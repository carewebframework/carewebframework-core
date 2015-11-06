/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.processor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.iterator.IResourceIterator;
import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.transform.AbstractTransform;

/**
 * Processes input resources via one or more transforms copying each result into the staging folder.
 * 
 * @param <T> Maven mojo class.
 */
public abstract class AbstractProcessor<T extends BaseMojo> {
    
    /**
     * A transform and its file filter.
     */
    private static class Transform {
        
        private final FileFilter filter;
        
        private final AbstractTransform transform;
        
        Transform(FileFilter filter, AbstractTransform transform) {
            this.filter = filter;
            this.transform = transform;
        }
    }
    
    private static final Pattern URL_PATTERN = Pattern.compile("~\\./");
    
    protected final T mojo;
    
    private final List<Transform> transforms = new ArrayList<>();
    
    /**
     * @param mojo The Maven base mojo.
     * @throws Exception Unspecified exception.
     */
    public AbstractProcessor(T mojo) throws Exception {
        this.mojo = mojo;
    }
    
    public abstract String relocateResource(String resourceName);
    
    public abstract String getResourceBase();
    
    public abstract void transform() throws Exception;
    
    public T getMojo() {
        return mojo;
    }
    
    public void transform(IResourceIterator resourceIterator) throws Exception {
        try {
            while (resourceIterator.hasNext()) {
                transform(resourceIterator.next());
            }
        } finally {
            resourceIterator.close();
        }
    }
    
    /**
     * Registers a file transform.
     * 
     * @param pattern One or more file patterns separated by commas.
     * @param transform The transform.
     */
    public void registerTransform(String pattern, AbstractTransform transform) {
        transforms.add(new Transform(new WildcardFileFilter(pattern.split("\\,")), transform));
    }
    
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
            
            if (url == null || (!mojo.isExcluded(url) && getTransform(url) != null)) {
                matcher.appendReplacement(sb, newPath);
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Finds and executes the transform appropriate for the file resource.
     * 
     * @param resource The file resource.
     * @return True if a processor was found for the jar entry.
     * @throws Exception Unspecified exception.
     */
    protected boolean transform(IResource resource) throws Exception {
        String name = StringUtils.trimToEmpty(resource.getSourcePath());
        
        if (resource.isDirectory() || mojo.isExcluded(name)) {
            return false;
        }
        
        AbstractTransform transform = getTransform(name);
        String targetPath = transform == null ? null : transform.getTargetPath(resource);
        
        if (targetPath != null) {
            File out = mojo.newStagingFile(relocateResource(targetPath), resource.getTime());
            try (OutputStream outputStream = new FileOutputStream(out);) {
                transform.transform(resource, outputStream);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the transform for the file, or null if none registered.
     * 
     * @param fileName The file name.
     * @return The associated transform, or null if not found.
     */
    private AbstractTransform getTransform(String fileName) {
        File file = new File(fileName);
        
        for (Transform transform : transforms) {
            if (transform.filter.accept(file)) {
                return transform.transform;
            }
        }
        
        return null;
    }
    
}
