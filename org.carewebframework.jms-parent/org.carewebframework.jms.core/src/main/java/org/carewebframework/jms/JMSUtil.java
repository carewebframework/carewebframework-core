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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.spring.SpringUtil;

import org.springframework.jms.core.JmsTemplate;

/**
 * JMS utility methods.
 */
public final class JMSUtil {
    
    /**
     * Enforce static class.
     */
    private JMSUtil() {
    }
    
    /**
     * Message property. For use in message selectors.
     */
    public static final String EVENT_RECIPIENTS_PROPERTY = "Recipients";
    
    /**
     * Message property. For use in message selectors.
     */
    public static final String EVENT_SENDER_PROPERTY = "Sender";
    
    private static final String JMS_TOPIC_TEMPLATE = "jmsTopicTemplate";
    
    private static final String JMS_QUEUE_TEMPLATE = "jmsQueueTemplate";
    
    /**
     * @return JmsTemplate
     */
    public static JmsTemplate getJmsTopicTemplate() {
        return SpringUtil.getBean(JMS_TOPIC_TEMPLATE, JmsTemplate.class);
    }
    
    /**
     * @return JmsTemplate
     */
    public static JmsTemplate getJmsQueueTemplate() {
        return SpringUtil.getBean(JMS_QUEUE_TEMPLATE, JmsTemplate.class);
    }
    
    /**
     * Extracts the topic name from an event name.
     * 
     * @param eventName Event name.
     * @return Topic name (highest level of event hierarchy).
     */
    public static String getTopicName(final String eventName) {
        final int i = eventName.indexOf('.');
        return i < 0 ? eventName : eventName.substring(0, i);
    }
    
    /**
     * Creates a message selector which considers JMSType and recipients properties.
     * 
     * @param eventName The event name (i.e. DESKTOP.LOCK).
     * @param recipientId The recipient id of the endpoint.
     * @return The message selector.
     */
    public static String getMessageSelector(final String eventName, final String recipientId) {
        return "(JMSType='" + eventName + "' OR JMSType LIKE '" + eventName + ".%') AND "
                + "(Recipients IS NULL OR Recipients LIKE '%," + recipientId + ",%')";
    }
    
    /**
     * Creates an ObjectMessage from a given session and sets properties of the message (JMSType,
     * {@value #EVENT_SENDER_PROPERTY}, {@value #EVENT_RECIPIENTS_PROPERTY}.
     * 
     * @param session The session for which to create the message.
     * @param jmsType Message's JMSType.
     * @param messageData Message data.
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return MessageThe newly created message.
     * @throws JMSException if error thrown from creation of object message
     */
    public static Message createObjectMessage(final Session session, final String jmsType, final Serializable messageData,
                                              final String sender, final String recipients) throws JMSException {
        return decorateMessage(session.createObjectMessage(messageData), jmsType, sender, recipients);
    }
    
    /**
     * Creates a TextMessage from a given session and sets properties of the message (JMSType,
     * {@value #EVENT_SENDER_PROPERTY}, {@value #EVENT_RECIPIENTS_PROPERTY}.
     * 
     * @param session the session for which to create the message
     * @param jmsType Message's JMSType
     * @param text text data
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return Message
     * @throws JMSException if error thrown from creation of object message
     */
    public static Message createTextMessage(final Session session, final String jmsType, final String text,
                                            final String sender, final String recipients) throws JMSException {
        return decorateMessage(session.createTextMessage(text), jmsType, sender, recipients);
    }
    
    /**
     * Given a Message, supplement the message with additional properties/attributes (JMSType,
     * recipients, sender).
     * 
     * @param message The message
     * @param jmsType JMSType
     * @param sender Sender client ID.
     * @param recipients Comma-delimited list of recipient client IDs
     * @return The decorated Message
     * @throws JMSException if error thrown setting properties
     */
    public static Message decorateMessage(final Message message, final String jmsType, final String sender,
                                          final String recipients) throws JMSException {
        message.setJMSType(jmsType);
        message.setStringProperty(EVENT_SENDER_PROPERTY, sender);
        message.setStringProperty(EVENT_RECIPIENTS_PROPERTY, StringUtils.isEmpty(recipients) ? null : "," + recipients + ",");
        return message;
    }
    
}
