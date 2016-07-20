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
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;

/**
 * Cache for resources returned by pattern matcher. Improves startup time of child application
 * contexts by caching resources after the initial lookup. Also, ensures that the returned resources
 * follow a predictable ordering.
 */
public class ResourceCache extends ConcurrentHashMap<String, Resource[]> {
    
    private static final long serialVersionUID = 1L;
    
    public interface IResourceCacheAware {
        
        /**
         * Retrieve resources based on the specified location pattern for storage in the cache.
         * 
         * @param locationPattern The location pattern.
         * @return An array of discovered resources.
         * @throws IOException IO exception.
         */
        public Resource[] getResourcesForCache(String locationPattern) throws IOException;
    }
    
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
    
    /**
     * Returns an array of resources corresponding to the specified pattern. If the pattern has not
     * yet been cached, the resources will be enumerated by the application context and stored in
     * the cache.
     * 
     * @param pattern Pattern to be used to lookup resources.
     * @param ctx Resource cache-aware application context to be used to lookup resources if not in
     *            cache.
     * @return An array of matching resources, sorted alphabetically by file name.
     * @throws IOException IO exception.
     */
    public Resource[] get(String pattern, IResourceCacheAware ctx) throws IOException {
        Resource[] resources = get(pattern);
        return resources == null ? internalGet(pattern, ctx) : resources;
    }
    
    /**
     * Use application context to enumerate resources. This call is thread safe.
     * 
     * @param pattern Pattern to be used to lookup resources.
     * @param ctx Resource cache-aware application context to be used to lookup resources.
     * @return An array of matching resources, sorted alphabetically by file name.
     * @throws IOException IO exception.
     */
    private synchronized Resource[] internalGet(String pattern, IResourceCacheAware ctx) throws IOException {
        Resource[] resources = get(pattern);
        
        if (resources != null) {
            return resources;
        }
        
        resources = ctx.getResourcesForCache(pattern);
        
        if (resources == null) {
            resources = new Resource[0];
        } else {
            Arrays.sort(resources, resourceComparator);
        }
        
        put(pattern, resources);
        return resources;
    }
    
}
