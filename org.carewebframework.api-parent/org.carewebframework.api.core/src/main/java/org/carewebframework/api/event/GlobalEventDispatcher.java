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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.messaging.ConsumerService;
import org.carewebframework.api.messaging.IMessageConsumer;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.messaging.ProducerService;
import org.carewebframework.api.security.SecurityUtil;

/**
 * Handles global dispatch of events via the messaging framework.
 */
public class GlobalEventDispatcher implements IGlobalEventDispatcher, IMessageConsumer.IMessageCallback {
    
    private PingEventHandler pingEventHandler;
    
    protected final IUser user;
    
    private final ILocalEventDispatcher localEventDispatcher;
    
    protected final PublisherInfo publisherInfo = new PublisherInfo();
    
    private final ProducerService producer;
    
    private final ConsumerService consumer;
    
    private String appName;
    
    /**
     * Create the global event dispatcher.
     */
    public GlobalEventDispatcher(ILocalEventDispatcher localEventDispatcher, ProducerService producer,
        ConsumerService consumer) {
        super();
        this.localEventDispatcher = localEventDispatcher;
        this.producer = producer;
        this.consumer = consumer;
        user = SecurityUtil.getAuthenticatedUser();
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    public void init() {
        publisherInfo.setEndpointId(getEndpointId());
        publisherInfo.setUserId(user == null ? null : user.getLogicalId());
        publisherInfo.setUserName(user == null ? "" : user.getFullName());
        publisherInfo.setNodeId(getNodeId());
        publisherInfo.setAppName(getAppName());
        localEventDispatcher.setGlobalEventDispatcher(this);
        pingEventHandler = new PingEventHandler((IEventManager) localEventDispatcher, publisherInfo);
        pingEventHandler.init();
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
     * Process a host event subscribe/unsubscribe request.
     */
    @Override
    public void subscribeRemoteEvent(String eventName, boolean subscribe) {
        String channelName = EventUtil.getChannelName(eventName);
        
        if (subscribe) {
            consumer.subscribe(channelName, this);
        } else {
            consumer.unsubscribe(channelName, this);
        }
    }
    
    /**
     * Fires a remote event.
     *
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients List of recipients for the event (null or empty string means all
     *            subscribers). A recipient may be an endpoint, a user, an application, a node, or a
     *            custom selector.
     */
    @Override
    public void fireRemoteEvent(String eventName, Serializable eventData, String recipients) {
        Message message = new EventMessage(eventName, eventData);
        producer.publish(message);
    }
    
    /**
     * Returns information about this publisher.
     */
    @Override
    public IPublisherInfo getPublisherInfo() {
        return publisherInfo;
    }
    
    /**
     * Gets the unique id for this end point.
     *
     * @return The end point's unique id.
     */
    protected String getEndpointId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Returns the node id. The default implementation will return null.
     *
     * @return The node id.
     */
    protected String getNodeId() {
        return null;
    }
    
    /**
     * Returns the application name.
     *
     * @return Application name.
     */
    protected String getAppName() {
        if (appName == null && FrameworkUtil.isInitialized()) {
            setAppName(FrameworkUtil.getAppName());
        }
        
        return appName;
    }
    
    /**
     * Allow for IOC injection of application name.
     *
     * @param appName Application name.
     */
    public void setAppName(String appName) {
        this.appName = StringUtils.replace(appName, ",", " ");
    }
    
    /**
     * Delivery the event to local subscribers. This may be overridden to provide alternate means
     * for delivering events.
     *
     * @param eventName The name of the event.
     * @param eventData Data associated with the event.
     */
    protected void localEventDelivery(String eventName, Object eventData) {
        localEventDispatcher.fireLocalEvent(eventName, eventData);
    }
    
    /**
     * Sends a CONNECT/DISCONNECT event for subscribers
     *
     * @param connected If true, send a CONNECT event. If false, send a DISCONNECT event.
     */
    protected void updateConnectionStatus(boolean connected) {
        fireRemoteEvent(connected ? "CONNECT" : "DISCONNECT", publisherInfo, null);
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
    
    @Override
    public void onMessage(Message message) {
        localEventDelivery(message.getType(), message.getPayload());
    }
    
}
