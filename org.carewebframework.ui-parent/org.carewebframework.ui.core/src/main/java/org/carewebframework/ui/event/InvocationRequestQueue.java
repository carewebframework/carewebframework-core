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
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;

/**
 * This class implements a queue that allows one page to execute methods on a target on another page
 * (the owner of the queue) via invocation requests.
 */
public class InvocationRequestQueue implements IEventListener {
    
    private static final int TIMEOUT_INTERVAL = 10000; // Timeout interval in milliseconds
    
    private final Object target;
    
    private final Page page;
    
    private final InvocationRequest onClose;
    
    private boolean closed;
    
    private long lastKeepAlive;
    
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
     * @param target Target of invocation requests sent to the queue.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(BaseComponent target, InvocationRequest onClose) {
        this(target.getPage(), target, onClose);
    }
    
    /**
     * Create a help message queue for the specified page and target.
     * 
     * @param page Page instance that owns the queue.
     * @param target Target of requests sent to the queue.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(Page page, Object target, InvocationRequest onClose) {
        super();
        this.target = target;
        this.page = page;
        this.onClose = onClose;
        resetKeepAlive();
        page.addEventListener("invoke", this);
    }
    
    /**
     * Close the invocation queue.
     */
    protected void close() {
        if (!closed) {
            closed = true;
            page.removeEventListener("invoke", this);
            
            if (onClose != null) {
                onEvent(onClose);
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
        EventUtil.post(page, request);
    }
    
    /**
     * Returns true if this queue is alive.
     * 
     * @return True if this queue is alive.
     */
    public boolean isAlive() {
        checkKeepAlive();
        return !closed && !page.isDead();
    }
    
    /**
     * Resets the keep-alive timer.
     */
    private void resetKeepAlive() {
        lastKeepAlive = System.currentTimeMillis();
    }
    
    /**
     * If keep-alive is enabled, check to see if it has exceeded the threshold. If it has, it is
     * assumed that the owner page is no longer valid and closes the queue.
     */
    private void checkKeepAlive() {
        if (!closed && System.currentTimeMillis() - lastKeepAlive > TIMEOUT_INTERVAL) {
            close();
        }
    }
    
    /**
     * Invokes the method on the target as specified by the event.
     * 
     * @param request The invocation request.
     */
    @Override
    public void onEvent(Event event) {
        try {
            InvocationRequest request = (InvocationRequest) event;
            MethodUtils.invokeMethod(target, request.getMethodName(), request.getArgs());
        } catch (Throwable e) {}
    }
}
