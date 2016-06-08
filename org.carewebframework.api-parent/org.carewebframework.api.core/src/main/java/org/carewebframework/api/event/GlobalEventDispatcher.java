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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.messaging.ConsumerService;
import org.carewebframework.api.messaging.IMessageConsumer;
import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.messaging.MessageUtil;
import org.carewebframework.api.messaging.ProducerService;
import org.carewebframework.api.messaging.PublisherInfo;
import org.carewebframework.api.messaging.Recipient;
import org.carewebframework.api.messaging.Recipient.RecipientType;
import org.carewebframework.api.security.SecurityUtil;

/**
 * Handles global dispatch of events via the messaging framework.
 */
public class GlobalEventDispatcher implements IGlobalEventDispatcher, IMessageConsumer.IMessageCallback {
    
    private PingEventHandler pingEventHandler;
    
    private final ILocalEventDispatcher localEventDispatcher;
    
    private final PublisherInfo publisherInfo = new PublisherInfo();
    
    private final String sessionId = UUID.randomUUID().toString();
    
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
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    public void init() {
        IUser user = SecurityUtil.getAuthenticatedUser();
        publisherInfo.setUserId(user == null ? null : user.getLogicalId());
        publisherInfo.setUserName(user == null ? "" : user.getFullName());
        publisherInfo.setAppName(getAppName());
        publisherInfo.setConsumerId(consumer.getNodeId());
        publisherInfo.setProducerId(producer.getNodeId());
        publisherInfo.setSessionId(sessionId);
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
     * Queues the specified event for delivery via the messaging service.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients Optional list of recipients for the event.
     */
    @Override
    public void fireRemoteEvent(String eventName, Serializable eventData, Recipient... recipients) {
        Message message = new EventMessage(eventName, eventData);
        producer.publish(EventUtil.getChannelName(eventName), message, recipients);
    }
    
    /**
     * Returns information about this publisher.
     */
    @Override
    public IPublisherInfo getPublisherInfo() {
        return publisherInfo;
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
        fireRemoteEvent(connected ? "CONNECT" : "DISCONNECT", publisherInfo);
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
    public void onMessage(String channel, Message message) {
        if (!isMessageExcluded(message)) {
            localEventDelivery(message.getType(), message.getPayload());
        }
    }
    
    private boolean isMessageExcluded(Message message) {
        return MessageUtil.isMessageExcluded(message, RecipientType.USER, publisherInfo.getUserId())
                || MessageUtil.isMessageExcluded(message, RecipientType.APPLICATION, publisherInfo.getAppName())
                || MessageUtil.isMessageExcluded(message, RecipientType.SESSION, publisherInfo.getSessionId());
    }
    
    @Override
    public void Ping(String responseEvent, List<PingFilter> filters, Recipient... recipients) {
        Recipient requestor = new Recipient(RecipientType.CONSUMER, getPublisherInfo().getConsumerId());
        PingRequest pingRequest = new PingRequest(responseEvent, filters, requestor);
        fireRemoteEvent(PingEventHandler.EVENT_PING_REQUEST, pingRequest, recipients);
    }
    
}
