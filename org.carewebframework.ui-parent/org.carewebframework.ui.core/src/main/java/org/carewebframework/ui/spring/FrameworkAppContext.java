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
package org.carewebframework.ui.spring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.carewebframework.api.spring.Constants;
import org.carewebframework.api.spring.DomainPropertySource;
import org.carewebframework.api.spring.FrameworkBeanFactory;
import org.carewebframework.api.spring.LabelPropertySource;
import org.carewebframework.api.spring.ResourceCache;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Subclass XmlWebApplicationContext to disable bean definition override capability. There is
 * currently no more direct way to do this for the root container.
 * <p>
 * By disabling bean definition overriding, bean id name collisions will result in an exception when
 * the bean definition is processed.
 */

public class FrameworkAppContext extends XmlWebApplicationContext implements ResourceCache.IResourceCacheAware {
    
    private static final ResourceCache resourceCache = new ResourceCache();
    
    private final Page page;
    
    private ContextClosedListener ctxListener;
    
    private class ContextClosedListener implements ApplicationListener<ContextClosedEvent> {
        
        /**
         * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
         */
        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            if (event.getSource().equals(getParent())) { //root context is closed
                close();
            } else if (event.getSource().equals(FrameworkAppContext.this)) {
                getParent().getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class)
                        .removeApplicationListener(FrameworkAppContext.this.ctxListener);
            }
        }
    }
    
    /**
     * Creates a root application context.
     */
    public FrameworkAppContext() {
        this(null, false);
    }
    
    /**
     * Constructor for creating an application context. Disallows bean overrides by default.
     * 
     * @param page The page associated with this application context. Will be null for the root
     *            application context.
     */
    public FrameworkAppContext(Page page) {
        this(page, false);
    }
    
    /**
     * Constructor for creating an application context. Disallows bean overrides by default.
     * 
     * @param page The page associated with this application context. Will be null for the root
     *            application context.
     * @param testConfig If true, use test profiles.
     * @param locations Optional list of configuration file locations. If not specified, defaults to
     *            the default configuration locations ({@link #getDefaultConfigLocations}).
     */
    public FrameworkAppContext(Page page, boolean testConfig, String... locations) {
        super();
        setAllowBeanDefinitionOverriding(true);
        this.page = page;
        ConfigurableEnvironment env = getEnvironment();
        Set<String> aps = new LinkedHashSet<>();
        Collections.addAll(aps, env.getActiveProfiles());
        
        if (page != null) {
            page.setAttribute(AppContextFinder.APP_CONTEXT_ATTRIB, this);
            ServletContext sc = ExecutionContext.getSession().getServletContext();
            WebApplicationContext rootContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
            setDisplayName("Child XmlWebApplicationContext " + page);
            setParent(rootContext);
            setServletContext(sc);
            this.ctxListener = new ContextClosedListener();
            getParent().getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class)
                    .addApplicationListener(this.ctxListener);
            // Set up profiles (remove root profiles merged from parent)
            aps.removeAll(Arrays.asList(Constants.PROFILES_ROOT));
            Collections.addAll(aps, testConfig ? Constants.PROFILES_PAGE_TEST : Constants.PROFILES_PAGE_PROD);
            env.setDefaultProfiles(Constants.PROFILE_PAGE_DEFAULT);
        } else {
            AppContextFinder.rootContext = this;
            Collections.addAll(aps, testConfig ? Constants.PROFILES_ROOT_TEST : Constants.PROFILES_ROOT_PROD);
            env.getPropertySources().addLast(new LabelPropertySource());
            env.getPropertySources().addLast(new DomainPropertySource(this));
            env.setDefaultProfiles(Constants.PROFILE_ROOT_DEFAULT);
        }
        
        env.setActiveProfiles(aps.toArray(new String[aps.size()]));
        setConfigLocations(locations == null || locations.length == 0 ? null : locations);
    }
    
    /**
     * Returns true if this is a root application context.
     * 
     * @return True if this is a root context.
     */
    public boolean isRoot() {
        return page == null;
    }
    
    /**
     * Remove page reference if any.
     */
    @Override
    public void close() {
        super.close();
        
        if (page != null) {
            page.removeAttribute(AppContextFinder.APP_CONTEXT_ATTRIB);
        }
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
     * For the root container, adds the usual defaults to any custom locations. For a child
     * container, returns just the usual defaults.
     */
    @Override
    protected String[] getDefaultConfigLocations() {
        return page == null ? (String[]) ArrayUtils.addAll(Constants.DEFAULT_LOCATIONS, super.getDefaultConfigLocations())
                : Constants.DEFAULT_LOCATIONS;
    }
    
    /**
     * Override to return a custom bean factory. Also, for page context, registers a page bean into
     * application context and registers any placeholder configurers found in parent context with
     * the child context (which allows resolution of placeholder values in the child context using a
     * common configurer).
     */
    @Override
    public DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory factory = new FrameworkBeanFactory(getParent(), getInternalParentBeanFactory());
        
        if (page != null) {
            factory.registerSingleton("page", page);
        }
        
        return factory;
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
     * @throws IOException if problem occurs
     */
    @Override
    public Resource[] getResourcesForCache(String locationPattern) throws IOException {
        return super.getResources(locationPattern);
    }
    
}
