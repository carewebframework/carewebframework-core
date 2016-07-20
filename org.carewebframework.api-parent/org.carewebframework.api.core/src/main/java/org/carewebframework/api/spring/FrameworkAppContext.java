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

import org.apache.commons.lang.ArrayUtils;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

/**
 * Subclass ClassPathXmlApplicationContext to disable bean definition override capability. There is
 * currently no more direct way to do this for the root container.
 * <p>
 * By disabling bean definition overriding we can implement more granular control over when to allow
 * bean definition overriding.
 */
public class FrameworkAppContext extends ClassPathXmlApplicationContext implements ResourceCache.IResourceCacheAware {
    
    private static final ResourceCache resourceCache = new ResourceCache();
    
    public FrameworkAppContext() {
        this(false);
    }
    
    /**
     * Constructor for creating an application context. Disallows bean overrides by default.
     * 
     * @param testConfig If true, use test profiles. If false, use production profiles.
     * @param locations Optional list of configuration file locations. If not specified, defaults to
     *            the default configuration locations ({@link #getDefaultConfigLocations}).
     */
    public FrameworkAppContext(boolean testConfig, String... locations) {
        super();
        setAllowBeanDefinitionOverriding(false);
        setConfigLocations(locations == null || locations.length == 0 ? null : locations);
        ConfigurableEnvironment env = getEnvironment();
        env.setActiveProfiles(testConfig ? Constants.PROFILES_TEST : Constants.PROFILES_PROD);
        env.getPropertySources().addLast(new LabelPropertySource());
        env.getPropertySources().addLast(new DomainPropertySource(this));
    }
    
    /**
     * Returns the default configuration locations.
     */
    @Override
    protected String[] getDefaultConfigLocations() {
        return Constants.DEFAULT_LOCATIONS;
    }
    
    /**
     * Adds one or more locations to the list of current locations to search for configuration
     * files.
     * 
     * @param location One or more locations to search.
     */
    public void addConfigLocation(String... location) {
        setConfigLocations((String[]) ArrayUtils.addAll(getConfigLocations(), location));
    }
    
    /**
     * Override to return a custom bean factory. Registers any placeholder configurers found in
     * parent context with the child context (which allows resolution of placeholder values in the
     * child context using a common configurer).
     */
    @Override
    public DefaultListableBeanFactory createBeanFactory() {
        return new FrameworkBeanFactory(getParent(), getInternalParentBeanFactory());
    }
    
    /**
     * Returns resources based on a location pattern. Overridden to support caching.
     */
    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        return resourceCache.get(locationPattern, this);
    }
    
    /**
     * Executes the super method of getResources.
     * 
     * @param locationPattern The resource search pattern.
     * @return An array of resources matching the specified pattern.
     * @throws IOException IO exception.
     */
    @Override
    public Resource[] getResourcesForCache(String locationPattern) throws IOException {
        return super.getResources(locationPattern);
    }
}
