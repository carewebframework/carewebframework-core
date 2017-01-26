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
package org.carewebframework.ui.event;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;

/**
 * This class implements a queue that allows one page to execute methods on a target on another page
 * (the owner of the queue) via invocation requests.
 */
public class InvocationRequestQueue {
    
    private static final int TIMEOUT_INTERVAL = 10000; // Timeout interval in milliseconds
    
    private static final Log log = LogFactory.getLog(InvocationRequestQueue.class);
    
    private final Object target;
    
    private final Page page;
    
    private final String name;
    
    private final String eventName;
    
    private final InvocationRequest onClose;
    
    private boolean closed;
    
    private final IEventListener invocationListener = new IEventListener() {
        
        /**
         * Invokes the method on the target as specified by the event.
         * 
         * @param event The invocation request event.
         */
        @Override
        public void onEvent(Event event) {
            invokeRequest((InvocationRequest) event.getData());
        }
    };
    
    /**
     * Create an invocation request.
     * 
     * @param methodName Name of method to invoke on the target.
     * @param args Arguments to pass to the invoked method.
     * @return The newly created invocation request.
     */
    public static InvocationRequest createRequest(String methodName, Object... args) {
        return new InvocationRequest(methodName, args);
    }
    
    /**
     * Create an invocation request queue for the specified target.
     * 
     * @param name Unique name for this queue.
     * @param target Target of invocation requests sent to the queue.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(String name, BaseComponent target, InvocationRequest onClose) {
        this(name, target.getPage(), target, onClose);
    }
    
    /**
     * Create a help message queue for the specified page and target.
     * 
     * @param name Unique name for this queue.
     * @param page Page instance that owns the queue.
     * @param target Target of requests sent to the queue.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(String name, Page page, Object target, InvocationRequest onClose) {
        super();
        this.name = name;
        this.target = target;
        this.page = page;
        this.onClose = onClose;
        eventName = "invoke_" + name;
        InvocationRequestQueueRegistry.getInstance().register(this);
        page.addEventListener(eventName, invocationListener);
    }
    
    public String getName() {
        return name;
    }
    
    private void invokeRequest(InvocationRequest request) {
        try {
            MethodUtils.invokeMethod(target, request.getMethodName(), request.getArgs());
        } catch (Exception e) {
            log.error("Remote invocation error.", e);
        }
    }
    
    /**
     * Close the invocation queue.
     */
    public void close() {
        if (!closed) {
            closed = true;
            InvocationRequestQueueRegistry.getInstance().unregister(this);
            page.removeEventListener(eventName, invocationListener);
            
            if (onClose != null) {
                invokeRequest(onClose);
            }
        }
    }
    
    /**
     * Queue a request.
     * 
     * @param methodName Name of method to invoke on the target.
     * @param args Arguments to pass to the invoked method.
     */
    public void sendRequest(String methodName, Object... args) {
        sendRequest(createRequest(methodName, args));
    }
    
    /**
     * Queue a request.
     * 
     * @param request The event packaging the request.
     */
    public void sendRequest(InvocationRequest request) {
        EventUtil.post(page, eventName, page, request);
        page.getSession().ping("wakeup");
    }
    
    /**
     * Returns true if this queue is alive.
     * 
     * @return True if this queue is alive.
     */
    public boolean isAlive() {
        if (!closed && page.isDead()) {
            close();
        }
        
        return !closed;
    }
    
}
