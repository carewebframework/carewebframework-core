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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.spring.FrameworkAppContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * This class creates a mock ZK environment suitable for certain kinds of unit tests. It creates a
 * web app instance with a single page and desktop and a mock session and execution. It also creates
 * a root Spring application context with a child desktop context.
 */
public class MockEnvironment {
    
    private Page page;
    
    private MockSynchronizer synchronizer;
    
    private MockWebSocketSession session;
    
    private MockClientRequest clientRequest;
    
    private ServletContext servletContext;
    
    private FrameworkAppContext rootContext;
    
    private FrameworkAppContext pageContext;
    
    private final Map<String, Object> browserInfo = new HashMap<>();
    
    private final Map<String, Object> clientRequestMap = new HashMap<>();
    
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
        servletContext = initServletContext(new MockServletContext());
        // Create root Spring context
        rootContext = initAppContext(new FrameworkAppContext(null, true), configLocations);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);
        rootContext.refresh();
        // Create mock session
        session = new MockWebSocketSession();
        synchronizer = new MockSynchronizer(session);
        // Create the page
        initBrowserInfoMap(browserInfo);
        page = Page._create("mockpage");
        Page._init(page, browserInfo, synchronizer);
        page = initPage(page);
        // Create the mock request
        initClientRequestMap(clientRequestMap);
        clientRequest = new MockClientRequest(clientRequestMap);
        // Create the mock execution
        initExecutionContext();
        // Create the desktop Spring context
        pageContext = initAppContext(new FrameworkAppContext(page, true), configLocations);
        pageContext.refresh();
    }
    
    /**
     * Cleans up all application contexts and invalidates the session.
     */
    public void close() {
        pageContext.close();
        page.destroy();
        IOUtils.closeQuietly(session);
        rootContext.close();
    }
    
    /**
     * Initialize the mock servlet context.
     * 
     * @param servletContext The mock servlet context.
     * @return The initialized mock servlet context.
     */
    protected MockServletContext initServletContext(MockServletContext servletContext) {
        return servletContext;
    }
    
    protected void initExecutionContext() {
        ExecutionContext.put(ExecutionContext.ATTR_WS, session);
        ExecutionContext.put(ExecutionContext.ATTR_SYNC, synchronizer);
        ExecutionContext.put(ExecutionContext.ATTR_SCTX, servletContext);
        ExecutionContext.put(ExecutionContext.ATTR_REQ, clientRequest);
    }
    
    protected void initClientRequestMap(Map<String, Object> map) {
        map.put("pid", page.getId());
        map.put("type", "mock");
    }
    
    /**
     * Initialize the app context.
     * 
     * @param appContext The app context.
     * @param configLocations Optional configuration locations.
     * @return The initialized app context.
     */
    protected FrameworkAppContext initAppContext(FrameworkAppContext appContext, String... configLocations) {
        appContext.setServletContext(servletContext);
        appContext.setConfigLocations(configLocations);
        return appContext;
    }
    
    /**
     * Initialize browserInfo map.
     * 
     * @param browserInfo The browser info map.
     */
    protected void initBrowserInfoMap(Map<String, Object> browserInfo) {
    }
    
    /**
     * Initialize the page.
     * 
     * @param page The page.
     * @return The initialized page.
     */
    protected Page initPage(Page page) {
        return page;
    }
    
    public FrameworkAppContext getPageContext() {
        return pageContext;
    }
    
    public FrameworkAppContext getRootContext() {
        return rootContext;
    }
    
    /**
     * First, posts any pending echo requests to the event queue. Then empties the event queue,
     * sending each queued event to its target. Finally, processes any events on the server push
     * event queue.
     * 
     * @return True if events were flushed.
     */
    public boolean flushEvents() {
        return false;
    }
    
}
