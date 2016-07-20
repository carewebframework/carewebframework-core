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
package org.carewebframework.messaging.jms;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.MiscUtil;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * JMX-based messaging service.
 */
@ManagedResource(description = "Runtime messaging support.")
public class JMSService {
    
    private static final Log log = LogFactory.getLog(JMSService.class);
    
    private Connection connection;
    
    private TopicSession session;
    
    private final JmsTemplate jmsTopicTemplate;
    
    private final JmsTemplate jmsQueueTemplate;
    
    private final ConnectionFactory factory;
    
    /**
     * Create the service.
     * 
     * @param jmsTopicTemplate The JMS topic template.
     * @param jmsQueueTemplate The JMP queue template.
     * @param factory The connection factory.
     */
    public JMSService(JmsTemplate jmsTopicTemplate, JmsTemplate jmsQueueTemplate, ConnectionFactory factory) {
        this.jmsTopicTemplate = jmsTopicTemplate;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.factory = factory;
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
            return true;
        } catch (Exception e) {
            log.error("Error communicating with JMS server: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * Disconnect from the JMS server.
     */
    private void disconnect() {
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
    
    /**
     * Initialize after setting all requisite properties.
     */
    public void init() {
        connect();
    }
    
    /**
     * Cleanup this instance.
     */
    public void destroy() {
        disconnect();
    }
    
    public Topic createTopic(String name) {
        try {
            return getSession().createTopic(name);
        } catch (JMSException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    public TopicSubscriber createSubscriber(Topic topic, String selector) {
        try {
            return getSession().createSubscriber(topic, selector, false);
        } catch (JMSException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Produces topic message. Uses JmsTemplate to send to local broker and forwards (based on
     * demand) to broker network.
     * 
     * @param destinationName The destination name.
     * @param messageData The message data.
     * @param recipients Comma-delimited list of recipient ids.
     */
    public void produceTopicMessage(String destinationName, String messageData, String recipients) {
        Message msg = createObjectMessage(messageData, "anonymous", recipients);
        sendMessage(destinationName, msg);
    }
    
    public void produceQueueMessage(String destinationName, String messageData) {
        Message msg = createObjectMessage(messageData, "anynomyous", null);
        jmsQueueTemplate.convertAndSend(destinationName, msg);
    }
    
    /**
     * Creates an ObjectMessage from a given session and sets properties of the message (JMSType,
     * {@value #MESSAGE_SENDER_PROPERTY}, {@value #MESSAGE_RECIPIENTS_PROPERTY}.
     * 
     * @param session The session for which to create the message.
     * @param messageData Message data.
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return MessageThe newly created message.
     * @throws JMSException if error thrown from creation of object message
     */
    public Message createObjectMessage(Serializable messageData, String sender, String recipients) {
        try {
            return decorateMessage(getSession().createObjectMessage(messageData), sender, recipients);
        } catch (JMSException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Creates a TextMessage from a given session and sets properties of the message (JMSType,
     * {@value #MESSAGE_SENDER_PROPERTY}, {@value #MESSAGE_RECIPIENTS_PROPERTY}.
     * 
     * @param session the session for which to create the message
     * @param text text data
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return Message
     * @throws JMSException if error thrown from creation of object message
     */
    public Message createTextMessage(String text, String sender, String recipients) throws JMSException {
        return decorateMessage(getSession().createTextMessage(text), sender, recipients);
    }
    
    /**
     * Given a Message, supplement the message with additional properties/attributes (JMSType,
     * recipients, sender).
     * 
     * @param message The message
     * @param topic Topic to which message will be published.
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return The decorated Message
     * @throws JMSException if error thrown setting properties
     */
    public Message decorateMessage(Message message, String sender, String recipients) throws JMSException {
        message.setStringProperty("sender", sender);
        message.setStringProperty("recipients", StringUtils.isEmpty(recipients) ? null : "," + recipients + ",");
        return message;
    }
    
    public void sendMessage(String destinationName, Message msg) {
        try {
            jmsTopicTemplate.convertAndSend(destinationName, msg);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    private TopicSession getSession() {
        connect();
        return session;
    }
}
