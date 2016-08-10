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
package org.carewebframework.ui.test;

import java.lang.reflect.Method;

import org.carewebframework.ui.ConsistentIdGenerator;
import org.carewebframework.ui.LifecycleEventDispatcher;
import org.carewebframework.web.spring.FrameworkAppContext;
import org.springframework.beans.BeanUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.http.SimpleWebApp;
import org.zkoss.zk.ui.impl.DesktopImpl;
import org.zkoss.zk.ui.impl.EventProcessor;
import org.zkoss.zk.ui.impl.PageImpl;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.sys.ConfigParser;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zk.ui.sys.SessionsCtrl;
import org.zkoss.zk.ui.util.Configuration;

/**
 * This class creates a mock ZK environment suitable for certain kinds of unit tests. It creates a
 * web app instance with a single page and desktop and a mock session and execution. It also creates
 * a root Spring application context with a child desktop context.
 */
public class MockEnvironment {
    
    private DesktopImpl desktop;
    
    private Session session;
    
    private MockExecution execution;
    
    private FrameworkAppContext rootContext;
    
    private FrameworkAppContext desktopContext;
    
    private SimpleWebApp webApp;
    
    private Configuration configuration;
    
    private MockHttpServletRequest request;
    
    private MockHttpServletResponse response;
    
    private MockServletContext servletContext;
    
    private MockServerPush serverPush;
    
    /**
     * Creates a mock environment for unit testing.
     */
    public MockEnvironment() {
    }
    
    /**
     * Initializes the mock environment.
     * 
     * @param configLocations Additional config file locations.
     * @throws Exception Unspecified exception.
     */
    public void init(String... configLocations) throws Exception {
        // Set up web app
        servletContext = init(new MockServletContext());
        configuration = init(new Configuration());
        webApp = init(new SimpleWebApp());
        // Create root Spring context
        rootContext = init(new FrameworkAppContext(null, true), configLocations);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);
        rootContext.refresh();
        // Create mock session
        request = init(new MockHttpServletRequest(servletContext));
        response = init(new MockHttpServletResponse());
        ServletRequestAttributes attribs = new ServletRequestAttributes(request, response);
        RequestContextHolder.setRequestAttributes(attribs);
        MockHttpSession nativeSession = init(new MockHttpSession(servletContext, "mock"));
        session = SessionsCtrl.newSession(webApp, nativeSession, request);
        request.setSession(nativeSession);
        SessionsCtrl.setCurrent(session);
        // Create the page
        PageImpl page = init(new PageImpl(LanguageDefinition.lookup(null), null, null, null));
        // Create the mock execution
        execution = init(new MockExecution(servletContext, request, response, null, page));
        // Create desktop
        ExecutionsCtrl.setCurrent(execution);
        desktop = init(new DesktopImpl(webApp, "mock", null, null, request));
        ExecutionsCtrl.setCurrent(null);
        // Initialize the environment
        webApp.getUiEngine().activate(execution);
        serverPush = init(new MockServerPush());
        desktop.enableServerPush(serverPush);
        page.preInit();
        page.init(init(new MockPageConfig()));
        inEventListener(true);
        // Create the desktop Spring context
        desktopContext = init(new FrameworkAppContext(desktop, true), configLocations);
        desktopContext.refresh();
    }
    
    /**
     * Cleans up all application contexts and invalidates the session.
     */
    public void close() {
        RequestContextHolder.resetRequestAttributes();
        desktopContext.close();
        desktop.destroy();
        session.invalidate();
        rootContext.close();
        webApp.destroy();
    }
    
    /**
     * Makes ZK believe the current thread is an event thread.
     * 
     * @param value If true, the current thread becomes an event thread. If false, it is not an
     *            event thread.
     * @throws Exception Unspecified exception.
     */
    public void inEventListener(boolean value) throws Exception {
        Method inEventListener = BeanUtils.findMethod(EventProcessor.class, "inEventListener", boolean.class);
        inEventListener.setAccessible(true);
        inEventListener.invoke(null, value);
    }
    
    /**
     * Initialize the mock servlet context.
     * 
     * @param servletContext The mock servlet context.
     * @return The initialized mock servlet context.
     */
    protected MockServletContext init(MockServletContext servletContext) {
        return servletContext;
    }
    
    /**
     * Initialize the configuration.
     * 
     * @param configuration The configuration.
     * @return The initialized configuration.
     * @throws Exception Unspecified exception.
     */
    protected Configuration init(Configuration configuration) throws Exception {
        new ConfigParser().parseConfigXml(configuration);
        configuration.addListener(LifecycleEventDispatcher.class);
        return configuration;
    }
    
    /**
     * Initialize the web app.
     * 
     * @param webApp The web app.
     * @return The initialized web app.
     */
    protected SimpleWebApp init(SimpleWebApp webApp) {
        webApp.setIdGenerator(new ConsistentIdGenerator());
        webApp.init(servletContext, configuration);
        return webApp;
    }
    
    /**
     * Initialize the app context.
     * 
     * @param appContext The app context.
     * @param configLocations Optional configuration locations.
     * @return The initialized app context.
     */
    protected FrameworkAppContext init(FrameworkAppContext appContext, String... configLocations) {
        appContext.setServletContext(servletContext);
        appContext.setConfigLocations(configLocations);
        return appContext;
    }
    
    /**
     * Initialize the mock session.
     * 
     * @param session The mock session.
     * @return The initialized mock session.
     */
    protected MockHttpSession init(MockHttpSession session) {
        return session;
    }
    
    /**
     * Initialize the page.
     * 
     * @param page The page.
     * @return The initialized page.
     */
    protected PageImpl init(PageImpl page) {
        return page;
    }
    
    /**
     * Initialize the mock execution.
     * 
     * @param execution The mock execution.
     * @return The initialized mock execution.
     */
    protected MockExecution init(MockExecution execution) {
        return execution;
    }
    
    /**
     * Initialize the desktop.
     * 
     * @param desktop The desktop.
     * @return The initialized desktop.
     */
    protected DesktopImpl init(DesktopImpl desktop) {
        return desktop;
    }
    
    /**
     * Initialize the mock server push.
     * 
     * @param serverPush The mock serverPush.
     * @return The initialized mock serverPush.
     */
    protected MockServerPush init(MockServerPush serverPush) {
        return serverPush;
    }
    
    /**
     * Initialize the mock servlet request.
     * 
     * @param request The mock request.
     * @return The initialized mock request.
     */
    protected MockHttpServletRequest init(MockHttpServletRequest request) {
        request.setRemoteAddr("127.0.0.1");
        request.setRemoteHost("mock");
        request.setRemotePort(8080);
        request.setRemoteUser("mockuser");
        request.setRequestURI("/zkau/mock");
        return request;
    }
    
    /**
     * Initialize the mock servlet response.
     * 
     * @param response The mock response.
     * @return The initialized mock response.
     */
    protected MockHttpServletResponse init(MockHttpServletResponse response) {
        return response;
    }
    
    /**
     * Initializes a mock page configuration.
     * 
     * @param pageConfig The mock page configuration.
     * @return The initialized mock page configuration.
     */
    protected MockPageConfig init(MockPageConfig pageConfig) {
        pageConfig.setViewport("auto");
        return pageConfig;
    }
    
    public Desktop getDesktop() {
        return desktop;
    }
    
    public Session getSession() {
        return session;
    }
    
    public Execution getExecution() {
        return execution;
    }
    
    public FrameworkAppContext getRootContext() {
        return rootContext;
    }
    
    public FrameworkAppContext getDesktopContext() {
        return desktopContext;
    }
    
    public WebApp getWebApp() {
        return webApp;
    }
    
    /**
     * First, posts any pending echo requests to the event queue. Then empties the event queue,
     * sending each queued event to its target. Finally, processes any events on the server push
     * event queue.
     * 
     * @return True if events were flushed.
     */
    public boolean flushEvents() {
        Event event;
        boolean result = false;
        
        for (AuEcho echo : execution.getEchoedEvents()) {
            Events.postEvent(toEvent(echo));
        }
        
        execution.getEchoedEvents().clear();
        
        while ((event = execution.getNextEvent()) != null) {
            Events.sendEvent(event);
            result = true;
        }
        
        result |= serverPush.flush();
        return result;
    }
    
    /**
     * Converts an echo response to the equivalent event.
     * 
     * @param echo An echo response.
     * @return Event as it would be echoed by client.
     */
    private Event toEvent(AuEcho echo) {
        Object[] raw = echo.getRawData();
        Component target = (Component) raw[0];
        String name = (String) raw[1];
        Object data = raw.length < 3 ? null : AuEcho.getData(target, raw[2]);
        return new Event(name, target, data);
    }
    
}
