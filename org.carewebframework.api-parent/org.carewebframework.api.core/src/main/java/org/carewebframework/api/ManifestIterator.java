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
package org.carewebframework.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fujion.common.StrUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Service that provides access to manifests for all components on the class path.
 */
public class ManifestIterator implements Iterable<Manifest>, ApplicationContextAware {

    private static class ManifestEx extends Manifest {

        private final String path;

        private ManifestEx(Resource resource) throws IOException {
            super(resource.getInputStream());
            path = StringUtils.removeEnd(resource.getURL().getPath(), MANIFEST_PATH);
            processFiles(resource, LICENSE_FILES, "License");
            processFiles(resource, README_FILES, "Description");
        }

        private void processFiles(Resource resource, String[] files, String attributeName) throws IOException {
            for (String file : files) {
                Resource res = resource.createRelative(file);

                if (res.exists()) {
                    String text = new String(IOUtils.toCharArray(res.getInputStream(), StrUtil.UTF8));
                    getMainAttributes().putValue(attributeName, text);
                    break;
                }
            }
        }
    }

    private static final ManifestIterator instance = new ManifestIterator();

    private static final Log log = LogFactory.getLog(ManifestIterator.class);

    protected static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private static final String[] LICENSE_FILES = { "LICENSE.TXT", "license.txt", "LICENSE", "license" };

    private static final String[] README_FILES = { "README.TXT", "readme.txt", "README", "readme" };

    private ApplicationContext applicationContext;
    
    private Manifest primaryManifest;

    private List<Manifest> manifests;

    /**
     * Returns the manifest iterator instance.
     *
     * @return The manifest iterator.
     */
    public static ManifestIterator getInstance() {
        return instance;
    }

    /**
     * Enforce singleton instance.
     */
    private ManifestIterator() {
        super();
    }

    /**
     * Initialize the manifest list if not already done. This is done by iterating over the class
     * path to locate all manifest files.
     */
    public void init() {
        if (manifests == null) {
            manifests = new ArrayList<>();
            
            try {
                primaryManifest = addToList(applicationContext.getResource(MANIFEST_PATH));
                Resource[] resources = applicationContext.getResources("classpath*:/" + MANIFEST_PATH);

                for (Resource resource : resources) {
                    Manifest manifest = addToList(resource);
                    
                    if (primaryManifest == null) {
                        primaryManifest = manifest;
                    }
                }

            } catch (Exception e) {
                log.error("Error enumerating manifests.", e);
            }

        }
    }

    /**
     * Returns a manifest based on the input path.
     *
     * @param path The path whose associated manifest is sought.
     * @return The manifest found on the specified path, or null if none.
     */
    public Manifest findByPath(String path) {
        for (Manifest manifest : this) {
            String mpath = ((ManifestEx) manifest).path;

            if (path.startsWith(mpath)) {
                return manifest;
            }
        }

        return null;
    }

    /**
     * Returns the iterator for all registered manifests.
     *
     * @return The manifest iterator.
     */
    @Override
    public Iterator<Manifest> iterator() {
        init();
        return manifests.iterator();
    }

    /**
     * Returns the primary manifest for the application, if any.
     *
     * @return The primary manifest, or null if not found.
     */
    public Manifest getPrimaryManifest() {
        init();
        return primaryManifest;
    }

    /**
     * Adds the manifest referenced by the specified resource to the list.
     *
     * @param resource Resource that references a manifest file.
     * @return The manifest that was added, or null if not found or an error occurred.
     */
    private Manifest addToList(Resource resource) {
        try {
            if (resource != null && resource.exists()) {
                Manifest manifest = new ManifestEx(resource);
                manifests.add(manifest);
                return manifest;
            }
        } catch (Exception e) {
            log.debug("Exception occurred reading manifest: " + resource);
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
