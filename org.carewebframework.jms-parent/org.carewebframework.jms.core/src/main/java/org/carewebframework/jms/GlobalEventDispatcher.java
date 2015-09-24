/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.jms;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.event.AbstractGlobalEventDispatcher;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * This class is responsible for communicating with the global messaging server (in this case, a JMS
 * server, but other implementations might use other technologies). It interacts with the local
 * event manager and is responsible for dispatching (publishing) events to be distributing globally
 * to the messaging server and receiving subscribed events from the same and passing them on to the
 * local event dispatcher for local distribution.
 * <p>
 * <i>Note: 2.6.1.RELEASE used SimpleMessageListenerContainer but was reverted due to
 * https://jira.springsource.org/browse/SPR-10397 . Below describes some of the differences between
 * Spring's support for message driven objects.</i>
 * <p>
 * SimpleMessageListenerContainer uses setMessageListener semantics - thus subject to connection
 * expiration. For example, if subscription to a Topic is requested and the connection times out
 * (and is closed), the subscription will also be removed. Whereas DefaultMessageListenerContainer
 * is more resistant to expiration/idle limits due to it's continuous polling. Use of
 * DefaultMessageListenerContainer has been deferred until we can evaluate the polling overhead (of
 * many containers) vs. SimpleMessageListenerContainer. Polling appears to require one thread per
 * container, so using this at the desktop scope adds threading overhead. Events received through
 * SimpleMessageListenerContainer are not transactional. Lastly, as event reception is not
 * transactional, a common ExecutorService is use to invoke the MessageListener, decoupling the
 * processing from receiving. Note: Acknowledgment is thus sent before the MessageListener callback
 * and therefore should not be used if MessageListener is changed to SessionAwareMessageListener or
 * when transaction support added. TODO Transactional Support.
 * <p>
 * TODO Transactional Support.
 */
public class GlobalEventDispatcher extends AbstractGlobalEventDispatcher implements MessageListener {
    
    private static final Log log = LogFactory.getLog(GlobalEventDispatcher.class);
    
    private final Map<String, TopicSubscriber> subscribers = Collections
            .synchronizedMap(new HashMap<String, TopicSubscriber>());
            
    private ConnectionFactory factory;
    
    private Connection connection;
    
    private TopicSession session;
    
    private JmsTemplate topicTemplate; //default "not transacted" and "auto-acknowledge"
    
    /**
     * Create the global event dispatcher.
     */
    public GlobalEventDispatcher() {
        super();
    }
    
    /**
     * Returns true if connected to JMS server
     * 
     * @return True if connected.
     */
    private boolean isConnected() {
        return this.connection != null;
    }
    
    /**
     * Connect to the JMS server.
     * 
     * @return True if successful.
     */
    private boolean connect() {
        if (this.factory == null) {
            return false;
        }
        
        if (isConnected()) {
            return true;
        }
        
        try {
            this.connection = this.factory.createConnection();
            this.session = (TopicSession) this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.connection.start();
            updateConnectionStatus(true);
            assertSubscriptions();
            return true;
        } catch (Exception e) {
            log.error("Error communicating with JMS server: " + e.getMessage());
            disconnect(false);
            return false;
        }
    }
    
    /**
     * Disconnect from the JMS server.
     * 
     * @param updateStatus If true, update the connection status.
     */
    private void disconnect(boolean updateStatus) {
        if (updateStatus) {
            updateConnectionStatus(false);
        }
        
        if (this.session != null) {
            try {
                this.session.close();
            } catch (Exception e) {
                log.error("Error closing JMS topic session.", e);
            }
        }
        
        if (this.connection != null) {
            try {
                this.connection.stop();
                this.connection.close();
            } catch (Exception e) {
                log.error("Error closing JMS topic connection.", e);
            }
        }
        
        this.session = null;
        this.connection = null;
    }
    
    @Override
    protected void updateConnectionStatus(boolean connected) {
        publisherInfo.setNodeId(getNodeId());
        super.updateConnectionStatus(connected);
    }
    
    @Override
    protected String getNodeId() {
        return JMSUtil.getClientId(connection);
    }
    
    /**
     * Initialize after setting all requisite properties.
     */
    @Override
    public void init() {
        super.init();
        connect();
    }
    
    /**
     * Cleanup this instance.
     */
    @Override
    public void destroy() {
        super.destroy();
        removeSubscriptions();
        disconnect(true);
    }
    
    /**
     * Sets the factory that will create connections for publishing and subscribing.
     * 
     * @param factory The ConnectionFactory
     */
    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Reassert subscriptions.
     */
    private void assertSubscriptions() {
        for (String eventName : this.subscribers.keySet()) {
            try {
                this.subscribers.put(eventName, null);
                doHostSubscribe(eventName);
            } catch (Throwable e) {
                break;
            }
        }
    }
    
    /**
     * Remove all remote subscriptions.
     */
    private void removeSubscriptions() {
        for (TopicSubscriber subscriber : this.subscribers.values()) {
            try {
                subscriber.close();
            } catch (Throwable e) {
                log.debug("Error closing subscriber", e);//is level appropriate - previously hidden exception -afranken
            }
        }
        
        this.subscribers.clear();
    }
    
    /**
     * Queue a subscription request.
     * 
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#subscribeRemoteEvent(java.lang.String,
     *      boolean)
     */
    @Override
    public void subscribeRemoteEvent(String eventName, boolean subscribe) {
        if (!isConnected()) {
            //AbstractGlobalEventDispatcher.init calls subscribe before subclass has a chance to initialize/connect
            connect();
        }
        try {
            if (subscribe) {
                doHostSubscribe(eventName);
            } else {
                doHostUnsubscribe(eventName);
            }
        } catch (JMSException e) {
            log.error(e);
        }
    }
    
    /**
     * Registers an event subscription with the global event manager. Note that the global event
     * manager has no knowledge of each event's individual subscribers - only that the event of a
     * given name has subscribers. This is because the global event manager need only dispatch
     * events to the local event manager. The local event manager will then dispatch events to the
     * individual subscribers.
     * 
     * @param eventName Name of event.
     * @throws JMSException JMS exception.
     */
    private void doHostSubscribe(String eventName) throws JMSException {
        
        if (this.subscribers.get(eventName) != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Already subscribed to Topic[%s]", eventName));
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Subscribing to Topic[%s]", eventName));
        }
        String topicName = JMSUtil.getTopicName(eventName);
        String selector = JMSUtil.getMessageSelector(eventName, getPublisherInfo());
        
        // This doesn't actually create a physical topic.  In ActiveMQ, a topic is created on-demand when someone with the
        // authority to create topics submits something to a topic.  By default, everyone has the authority to create topics.  See
        // http://markmail.org/message/us7v5ocnb65m4fdp#query:createtopic%20activemq%20jms+page:1+mid:tce6soq5g7rdkqnw+state:results --lrc
        Topic topic = this.session.createTopic(topicName);
        TopicSubscriber subscriber = this.session.createSubscriber(topic, selector, false);
        this.subscribers.put(eventName, subscriber);
        subscriber.setMessageListener(this);
    }
    
    /**
     * Removes an event subscription with the global event manager.
     * 
     * @param eventName Name of event
     * @throws JMSException JMS exception.
     */
    private void doHostUnsubscribe(String eventName) throws JMSException {
        TopicSubscriber subscriber = this.subscribers.remove(eventName);
        if (subscriber == null) {
            return;
        }
        log.debug(String.format("Unsubscribing Subscriber[%s] for Topic [%s].", subscriber, eventName));
        subscriber.close();
    }
    
    /**
     * @see org.carewebframework.api.event.IGlobalEventDispatcher#fireRemoteEvent(java.lang.String,
     *      java.io.Serializable, java.lang.String)
     */
    @Override
    public void fireRemoteEvent(String eventName, Serializable eventData, String recipients) {
        try {
            doFireRemoteEvent(eventName, eventData, recipients);
        } catch (JMSException e) {
            log.error("Error firing remote event.", e);
        }
    }
    
    /**
     * Publishes the specified event to the messaging server.
     * 
     * @param eventName Name of the event.
     * @param eventData Data object associated with the event.
     * @param recipients List of recipients for the event (null or empty string means all
     *            subscribers).
     * @throws JMSException JMS exception.
     */
    private void doFireRemoteEvent(final String eventName, final Object eventData,
                                   final String recipients) throws JMSException {
        this.topicTemplate.send(JMSUtil.getTopicName(eventName), new MessageCreator() {
            
            @Override
            public Message createMessage(Session session) throws JMSException {
                return JMSUtil.createObjectMessage(session, eventName, (Serializable) eventData, getEndpointId(),
                    recipients);
            }
        });
    }
    
    /**
     * This is the callback for messages received from the JMS server.
     * 
     * @param message Message received from the JMS server.
     */
    @Override
    public void onMessage(Message message) {
        if (log.isDebugEnabled()) {
            log.debug("Message received: " + message);
        }
        
        processMessage(message);
    }
    
    /**
     * Process a dequeued message by forwarding it to the local event manager for local delivery. If
     * the message is a ping request, send the response.
     * 
     * @param message Message to process.
     */
    protected void processMessage(Message message) {
        try {
            String eventName = message.getJMSType();
            Object eventData;
            
            if (message instanceof ObjectMessage) {
                eventData = ((ObjectMessage) message).getObject();
            } else if (message instanceof TextMessage) {
                eventData = ((TextMessage) message).getText();
            } else {
                log.warn(
                    String.format("Ignoring unsupported message: type [%s], message [%s]", message.getClass(), message));
                return;
            }
            localEventDelivery(eventName, eventData);
        } catch (Exception e) {
            log.error("Error during local dispatch of global event.", e);
        }
    }
    
    /**
     * Override to do any special setup prior to processing of messages.
     * 
     * @return True if OK to proceed.
     */
    @Override
    protected boolean beginMessageProcessing() {
        return true;
    }
    
    /**
     * Override to do any special teardown after processing of messages.
     */
    @Override
    protected void endMessageProcessing() {
    
    }
    
    /**
     * @param topicTemplate the jmsTemplate to set with pub sub config
     */
    public void setTopicTemplate(JmsTemplate topicTemplate) {
        this.topicTemplate = topicTemplate;
    }
    
}
