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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.util.DesktopCleanup;

/**
 * This class implements a queue that allows one desktop to execute methods on a target on another
 * desktop (the owner of the queue) via invocation requests.
 */
public class InvocationRequestQueue implements EventListener<InvocationRequest> {
    
    /**
     * Listens to desktop async requests.
     */
    private class DesktopListener implements AuService {
        
        /**
         * Resets the keep alive timer.
         * 
         * @see org.zkoss.zk.au.AuService#service(org.zkoss.zk.au.AuRequest, boolean)
         */
        @Override
        public boolean service(AuRequest request, boolean everError) {
            resetKeepAlive();
            return false;
        }
        
    }
    
    private static final Map<String, InvocationRequestQueue> messageQueues = new HashMap<>();
    
    private static final int TIMEOUT_INTERVAL = 10000; // Timeout interval in milliseconds
    
    private final Object target;
    
    private final EventQueue<InvocationRequest> eventQueue;
    
    private final Desktop desktop;
    
    private final String queueName;
    
    private final DesktopListener desktopListener = new DesktopListener();
    
    private final InvocationRequest onClose;
    
    private boolean closed;
    
    private long lastKeepAlive;
    
    /**
     * Looks up and returns the named queue registered with the specified owner.
     * 
     * @param ownerId This is the unique desktop id of the queue's owner.
     * @param queueName The queue name.
     * @return The requested message queue, or null if not found.
     */
    public static InvocationRequestQueue getQueue(String ownerId, String queueName) {
        synchronized (messageQueues) {
            return messageQueues.get(getQualifiedQueueName(ownerId, queueName));
        }
    }
    
    /**
     * Returns the queue name qualified by the owner id.
     * 
     * @param ownerId This is the unique desktop id of the queue's owner.
     * @param queueName The queue name.
     * @return The qualified queue name.
     */
    public static String getQualifiedQueueName(String ownerId, String queueName) {
        return ownerId + "_" + queueName;
    }
    
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
     * @param queueName The name of the queue to be created.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(Component target, String queueName, InvocationRequest onClose) {
        this(target.getDesktop(), target, queueName, onClose);
    }
    
    /**
     * Create a help message queue for the specified desktop and target.
     * 
     * @param desktop Desktop instance that owns the queue.
     * @param target Target of requests sent to the queue.
     * @param queueName The name of the queue to be created.
     * @param onClose Invocation request to send to the target upon queue closure (may be null).
     */
    public InvocationRequestQueue(Desktop desktop, Object target, String queueName, InvocationRequest onClose) {
        super();
        this.target = target;
        this.desktop = desktop;
        this.queueName = getQualifiedQueueName(desktop.getId(), queueName);
        this.onClose = onClose;
        resetKeepAlive();
        eventQueue = createQueue();
        desktop.addListener(new DesktopCleanup() {
            
            /**
             * Closes the invocation request queue when the owning desktop is destroyed.
             * 
             * @see org.zkoss.zk.ui.util.DesktopCleanup#cleanup(org.zkoss.zk.ui.Desktop)
             */
            @Override
            public void cleanup(Desktop desktop) throws Exception {
                close();
            }
            
        });
        
        desktop.addListener(desktopListener);
    }
    
    /**
     * Creates a queue for the specified owner and establishes a subscriber. The queue will be
     * automatically removed when the owning desktop is destroyed.
     * 
     * @return The newly created event queue.
     */
    private EventQueue<InvocationRequest> createQueue() {
        EventQueue<InvocationRequest> eventQueue = lookupQueue(true);
        eventQueue.subscribe(this);
        registerQueue();
        return eventQueue;
    }
    
    /**
     * Registers a message queue. A run time exception is thrown if a queue by the same name is
     * already registered for this desktop.
     */
    private void registerQueue() {
        synchronized (messageQueues) {
            if (messageQueues.get(queueName) == null) {
                messageQueues.put(queueName, this);
            } else {
                throw new RuntimeException("An invocation request queue '" + queueName
                        + "' already exists for this desktop.");
            }
        }
    }
    
    /**
     * Unregisters the message queue. If the queue is no longer registered, the request is ignored.
     */
    private void unregisterQueue() {
        synchronized (messageQueues) {
            if (messageQueues.get(queueName) == this) {
                messageQueues.remove(queueName);
            }
        }
    }
    
    /**
     * Close the message queue.
     */
    protected void close() {
        if (!closed) {
            closed = true;
            desktop.removeListener(desktopListener);
            unregisterQueue();
            EventQueues.remove(queueName, desktop.getWebApp());
            
            if (onClose != null) {
                onEvent(onClose);
            }
        }
    }
    
    /**
     * Returns the qualified name of the associated event queue.
     * 
     * @return The qualified queue name.
     */
    public String getQualifiedQueueName() {
        return queueName;
    }
    
    /**
     * Returns a reference to the associated queue.
     * 
     * @param autoCreate If true and the queue does not yet exist, it is created.
     * @return The associated event queue. May return null if autoCreate is false and the queue no
     *         longer exists.
     */
    private EventQueue<InvocationRequest> lookupQueue(boolean autoCreate) {
        return EventQueues.lookup(queueName, EventQueues.APPLICATION, autoCreate);
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
        eventQueue.publish(request);
    }
    
    /**
     * Returns true if this queue is alive.
     * 
     * @return True if this queue is alive.
     */
    public boolean isAlive() {
        checkKeepAlive();
        return !closed && desktop.isAlive() && lookupQueue(false) != null;
    }
    
    /**
     * Resets the keep-alive timer.
     */
    private void resetKeepAlive() {
        lastKeepAlive = System.currentTimeMillis();
    }
    
    /**
     * If keep-alive is enabled, check to see if it has exceeded the threshold. If it has, it is
     * assumed that the owner desktop is no longer valid and closes the queue.
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
    public void onEvent(InvocationRequest request) {
        try {
            MethodUtils.invokeMethod(target, request.getName(), request.getArgs());
        } catch (Throwable e) {}
    }
}
