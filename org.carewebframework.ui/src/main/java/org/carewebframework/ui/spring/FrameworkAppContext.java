/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.spring;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;

import org.carewebframework.api.spring.DomainPropertySource;
import org.carewebframework.api.spring.FrameworkBeanFactory;
import org.carewebframework.api.spring.ResourceCache;
import org.carewebframework.ui.util.MemoryLeakPreventionUtil;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;

/**
 * Subclass XmlWebApplicationContext to disable bean definition override capability. There is
 * currently no more direct way to do this for the root container.
 * <p>
 * By disabling bean definition overriding, bean id name collisions will result in an exception when
 * the bean definition is processed.
 */

public class FrameworkAppContext extends XmlWebApplicationContext implements ResourceCache.IResourceCacheAware {
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context.
     */
    public static final String PROFILE_ROOT = "root";
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context, in a production setting.
     */
    public static final String PROFILE_ROOT_PROD = "root-prod";
    
    /**
     * Constant for beans profile which identifies beans to be processed by Spring's root
     * application context, in a test setting.
     */
    public static final String PROFILE_ROOT_TEST = "root-test";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context.
     */
    public static final String PROFILE_DESKTOP = "desktop";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a production setting.
     */
    public static final String PROFILE_DESKTOP_PROD = "desktop-prod";
    
    /**
     * Constant for beans profile which identifies beans to be processed by a child Spring
     * application context, in a test setting.
     */
    public static final String PROFILE_DESKTOP_TEST = "desktop-test";
    
    /**
     * Default locations to search for configurations files for the root Spring application context.
     */
    private static final String[] DEFAULT_LOCATIONS_ROOT = { "classpath*:/META-INF/*-spring.xml",
            "classpath*:/META-INF/*-beans-init.xml" };
    
    /**
     * Default locations to search for configurations files for the child Spring application
     * context.
     */
    private static final String[] DEFAULT_LOCATIONS_DESKTOP = { "classpath*:/META-INF/*-spring.xml",
            "classpath*:/META-INF/*-beans.xml" };
    
    private static final String APP_CONTEXT_ATTRIB = "_CWFAppContext";
    
    private static final ResourceCache resourceCache = new ResourceCache();
    
    private final Desktop desktop;
    
    private ContextClosedListener ctxListener;
    
    /**
     * Returns the application context associated with the given desktop.
     * 
     * @param desktop Desktop instance.
     * @return Application context associated with the desktop.
     */
    public static FrameworkAppContext getAppContext(final Desktop desktop) {
        return desktop == null ? null : (FrameworkAppContext) desktop.getAttribute(APP_CONTEXT_ATTRIB);
    }
    
    private class ContextClosedListener implements ApplicationListener<ContextClosedEvent> {
        
        /**
         * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
         */
        @Override
        public void onApplicationEvent(final ContextClosedEvent event) {
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
     * @param desktop The desktop associated with this application context. Will be null for the
     *            root application context.
     */
    public FrameworkAppContext(Desktop desktop) {
        this(desktop, false);
    }
    
    /**
     * Constructor for creating an application context. Disallows bean overrides by default.
     * 
     * @param desktop The desktop associated with this application context. Will be null for the
     *            root application context.
     * @param testConfig If true, use test profiles.
     * @param locations Optional list of configuration file locations. If not specified, defaults to
     *            the default configuration locations ({@link #getDefaultConfigLocations}).
     */
    public FrameworkAppContext(Desktop desktop, boolean testConfig, String... locations) {
        super();
        setAllowBeanDefinitionOverriding(false);
        this.desktop = desktop;
        ConfigurableEnvironment env = getEnvironment();
        
        if (desktop != null) {
            desktop.setAttribute(APP_CONTEXT_ATTRIB, this);
            final Session session = desktop.getSession();
            final ServletContext sc = session.getWebApp().getServletContext();
            final WebApplicationContext rootContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
            setDisplayName("Child XmlWebApplicationContext " + desktop);
            setParent(rootContext);
            setServletContext(sc);
            this.ctxListener = new ContextClosedListener();
            getParent().getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class)
                    .addApplicationListener(this.ctxListener);
            // Set up profiles (remove root profiles merged from parent)
            Set<String> aps = new LinkedHashSet<String>();
            Collections.addAll(aps, env.getActiveProfiles());
            aps.remove(PROFILE_ROOT);
            aps.remove(PROFILE_ROOT_TEST);
            aps.remove(PROFILE_ROOT_PROD);
            aps.add(PROFILE_DESKTOP);
            aps.add(testConfig ? PROFILE_DESKTOP_TEST : PROFILE_DESKTOP_PROD);
            env.setActiveProfiles(aps.toArray(new String[aps.size()]));
        } else {
            AppContextFinder.rootContext = this;
            env.addActiveProfile(PROFILE_ROOT);
            env.addActiveProfile(testConfig ? PROFILE_ROOT_TEST : PROFILE_ROOT_PROD);
            env.getPropertySources().addLast(new LabelPropertySource(this));
            env.getPropertySources().addLast(new DomainPropertySource(this));
        }
        
        setConfigLocations(locations == null || locations.length == 0 ? null : locations);
    }
    
    /**
     * Returns true if this is a root application context.
     * 
     * @return True if this is a root context.
     */
    public boolean isRoot() {
        return desktop == null;
    }
    
    /**
     * Remove desktop reference if any.
     */
    @Override
    public void close() {
        super.close();
        
        if (desktop != null) {
            desktop.removeAttribute(APP_CONTEXT_ATTRIB);
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
     * For the root container, returns the usual defaults. For a child container, must always return
     * null.
     */
    @Override
    protected String[] getDefaultConfigLocations() {
        return desktop == null ? (String[]) ArrayUtils.addAll(DEFAULT_LOCATIONS_ROOT, super.getDefaultConfigLocations())
                : DEFAULT_LOCATIONS_DESKTOP;
    }
    
    /**
     * Override to return a custom bean factory. Also, for desktop context, registers a desktop bean
     * into application context and registers any placeholder configurers found in parent context
     * with the child context (which allows resolution of placeholder values in the child context
     * using a common configurer).
     */
    @Override
    public DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory factory = new FrameworkBeanFactory(getParent(), getInternalParentBeanFactory());
        
        if (desktop != null) {
            factory.registerSingleton("desktop", desktop);
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
    
    /**
     * @see org.springframework.context.support.AbstractApplicationContext#onClose()
     */
    @Override
    protected void onClose() {
        super.onClose();
        if (getParent() == null) {
            //final root context cleanup
            MemoryLeakPreventionUtil.clean();
        }
    }
}
