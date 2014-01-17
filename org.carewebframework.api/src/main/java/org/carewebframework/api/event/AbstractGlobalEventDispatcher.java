/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

import java.io.Serializable;
import java.util.UUID;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;

/**
 * Implement a subclass of this abstract base class to mediate communications with the global
 * messaging server (which might be a JMS-compliant messaging server or some other implementation
 * altogether). This base class provides interaction with the local event manager. Specific
 * implementations are responsible for dispatching (publishing) events to be distributed globally to
 * the messaging server and receiving subscribed events from the same and passing them on to the
 * local event dispatcher for local distribution.
 */
public abstract class AbstractGlobalEventDispatcher implements IGlobalEventDispatcher, IPublisherInfo {
    
    private ILocalEventDispatcher localEventDispatcher;
    
    private PingEventHandler pingEventHandler;
    
    private String appName;
    
    private final String endpointId = "e-" + UUID.randomUUID().toString();
    
    protected final IUser user;
    
    /**
     * Create the global event dispatcher.
     */
    public AbstractGlobalEventDispatcher() {
        super();
        user = SecurityUtil.getAuthenticatedUser();
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    public void init() {
        if (localEventDispatcher != null) {
            localEventDispatcher.setGlobalEventDispatcher(this);
            pingEventHandler = new PingEventHandler((IEventManager) localEventDispatcher, this);
            pingEventHandler.init();
        }
    }
    
    /**
     * Cleanup this instance.
     */
    public void destroy() {
        if (pingEventHandler != null) {
            pingEventHandler.destroy();
        }
    }
    
    /**
     * Sets the local event dispatcher to be used for dispatching events received from the remote
     * server to local subscribers.
     * 
     * @param localEventDispatcher
     */
    public void setLocalEventDispatcher(ILocalEventDispatcher localEventDispatcher) {
        this.localEventDispatcher = localEventDispatcher;
    }
    
    /**
     * Process a host event subscribe/unsubscribe request.
     */
    @Override
    public abstract void subscribeRemoteEvent(String eventName, boolean subscribe);
    
    /**
     * Fires a remote event.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param endpoints List of endpoints for the event (null or empty string means all
     *            subscribers).
     */
    @Override
    public abstract void fireRemoteEvent(String eventName, Serializable eventData, String endpoints);
    
    /**
     * Sets the application name.
     * 
     * @param appName The application name.
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /**
     * Returns the application name. If not explicitly set, retrieves the application name from the
     * framework.
     * 
     * @return The application name.
     */
    @Override
    public String getAppName() {
        if (appName == null) {
            appName = FrameworkUtil.isInitialized() ? FrameworkUtil.getAppName() : "";
        }
        
        return appName;
    }
    
    /**
     * Returns the user's id.
     */
    @Override
    public String getUserId() {
        return user == null ? null : "u-" + user.getDomainId();
    }
    
    /**
     * Returns the unique id for this end point.
     * 
     * @return The end point's unique id.
     */
    @Override
    public String getEndpointId() {
        return endpointId;
    }
    
    /**
     * Returns the unique id for this node.
     * 
     * @return The node id.
     */
    @Override
    public String getNodeId() {
        return null;
    }
    
    /**
     * Returns the user's full name.
     */
    @Override
    public String getUserName() {
        return user == null ? "" : user.getFullName();
    }
    
    /**
     * Delivery the event to local subscribers. This may be overridden to provide alternate means
     * for delivering events.
     * 
     * @param eventName The name of the event.
     * @param eventData Data associated with the event.
     */
    protected void localEventDelivery(String eventName, Object eventData) {
        if (localEventDispatcher != null) {
            localEventDispatcher.fireLocalEvent(eventName, eventData);
        }
    }
    
    /**
     * Sends a CONNECT/DISCONNECT event for subscribers
     * 
     * @param connected If true, send a CONNECT event. If false, send a DISCONNECT event.
     */
    protected void updateConnectionStatus(boolean connected) {
        fireRemoteEvent(connected ? "CONNECT" : "DISCONNECT", new PublisherInfo(this), null);
    }
    
    /**
     * Override to do any special setup prior to processing of messages.
     * 
     * @return True if OK to proceed.
     */
    protected boolean beginMessageProcessing() {
        return true;
    }
    
    /**
     * Override to do any special teardown after processing of messages.
     */
    protected void endMessageProcessing() {
        
    }
    
}
