/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
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
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.api.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.fujion.common.MiscUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * Takes an input resource and transforms it by resolving any embedded property values.
 */
public class PropertyAwareResource implements Resource, ApplicationContextAware {
    
    private final Resource originalResource;
    
    private Resource transformedResource;
    
    /**
     * @param resource Resource to be transformed.
     */
    public PropertyAwareResource(Resource resource) {
        originalResource = resource;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return transformedResource.getInputStream();
    }
    
    @Override
    public long contentLength() throws IOException {
        return transformedResource.contentLength();
    }
    
    @Override
    public Resource createRelative(String value) throws IOException {
        return transformedResource.createRelative(value);
    }
    
    @Override
    public boolean exists() {
        return transformedResource.exists();
    }
    
    @Override
    public String getDescription() {
        return transformedResource.getDescription();
    }
    
    @Override
    public File getFile() throws IOException {
        return transformedResource.getFile();
    }
    
    @Override
    public String getFilename() {
        return transformedResource.getFilename();
    }
    
    @Override
    public URI getURI() throws IOException {
        return transformedResource.getURI();
    }
    
    @Override
    public URL getURL() throws IOException {
        return transformedResource.getURL();
    }
    
    @Override
    public boolean isOpen() {
        return transformedResource.isOpen();
    }
    
    @Override
    public boolean isReadable() {
        return transformedResource.isReadable();
    }
    
    @Override
    public long lastModified() throws IOException {
        return transformedResource.lastModified();
    }
    
    /**
     * Use the application context to resolve any embedded property values within the original
     * resource.
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        try (InputStream is = originalResource.getInputStream();) {
            ConfigurableListableBeanFactory beanFactory = ((AbstractRefreshableApplicationContext) appContext)
                    .getBeanFactory();
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter = IOUtils.lineIterator(is, "UTF-8");
            boolean transformed = false;
            
            while (iter.hasNext()) {
                String line = iter.next();
                
                if (line.contains("${")) {
                    transformed = true;
                    line = beanFactory.resolveEmbeddedValue(line);
                }
                
                sb.append(line);
            }
            
            transformedResource = !transformed ? originalResource : new ByteArrayResource(sb.toString().getBytes());
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
