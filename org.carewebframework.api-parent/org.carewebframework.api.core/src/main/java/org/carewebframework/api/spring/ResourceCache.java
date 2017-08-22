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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.carewebframework.common.MiscUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Cache for resources returned by pattern matcher. Improves startup time of child application
 * contexts by caching resources after the initial lookup. Also, ensures that the returned resources
 * follow a predictable ordering.
 */
public class ResourceCache implements ResourcePatternResolver {

    private final Map<String, Resource[]> cache = new ConcurrentHashMap<>();

    private final ResourcePatternResolver resolver;

    /**
     * Provides a predictable ordering of context configuration resources.
     */
    public static final Comparator<Resource> resourceComparator = new Comparator<Resource>() {

        @Override
        public int compare(Resource r1, Resource r2) {
            String f1 = r1.getFilename();
            String f2 = r2.getFilename();
            return f1 == f2 ? 0 : f1 == null ? -1 : f2 == null ? 1 : f1.compareToIgnoreCase(f2);
        }

    };

    public ResourceCache(ResourceLoader resourceLoader) {
        this.resolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }

    /**
     * Returns an array of resources corresponding to the specified pattern. If the pattern has not
     * yet been cached, the resources will be enumerated by the application context and stored in
     * the cache.
     *
     * @param pattern Pattern to be used to lookup resources.
     * @return An array of matching resources, sorted alphabetically by file name.
     */
    @Override
    public Resource[] getResources(String pattern) {
        Resource[] resources = cache.get(pattern);
        return resources == null ? internalGet(pattern) : resources;
    }

    /**
     * Use application context to enumerate resources. This call is thread safe.
     *
     * @param pattern Pattern to be used to lookup resources.
     * @return An array of matching resources, sorted alphabetically by file name.
     */
    private synchronized Resource[] internalGet(String pattern) {
        Resource[] resources = cache.get(pattern);

        if (resources != null) {
            return resources;
        }

        try {
            resources = resolver.getResources(pattern);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }

        if (resources == null) {
            resources = new Resource[0];
        } else {
            Arrays.sort(resources, resourceComparator);
        }

        cache.put(pattern, resources);
        return resources;
    }

    @Override
    public Resource getResource(String location) {
        return resolver.getResource(location);
    }

    @Override
    public ClassLoader getClassLoader() {
        return resolver.getClassLoader();
    }

}
