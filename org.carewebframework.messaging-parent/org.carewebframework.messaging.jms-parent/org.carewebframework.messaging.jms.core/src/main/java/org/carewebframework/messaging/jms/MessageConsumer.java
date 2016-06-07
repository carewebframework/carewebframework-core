/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.messaging.jms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.messaging.IMessageConsumer;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.common.MiscUtil;

/**
 * JMS-based message consumer.
 */
public class MessageConsumer implements IMessageConsumer {
    
    private static final Log log = LogFactory.getLog(MessageConsumer.class);
    
    private class Subscriber implements MessageListener {
        
        private final String topic;
        
        Subscriber(String topic) {
            this.topic = topic;
        }
        
        @Override
        public void onMessage(javax.jms.Message message) {
            try {
                Object payload;
                
                if (message instanceof ObjectMessage) {
                    payload = ((ObjectMessage) message).getObject();
                } else if (message instanceof TextMessage) {
                    payload = ((TextMessage) message).getText();
                } else {
                    throw new Exception("Ignoring unsupported message");
                }
                
                Message msg = payload instanceof Message ? (Message) payload : new Message("jmsMessage", payload);
                
                if (callback != null) {
                    callback.onMessage(topic, msg);
                }
            } catch (Exception e) {
                log.warn(String.format("Error processing message: type [%s], message [%s]", message.getClass(), message), e);
            }
        }
        
    }
    
    private final Map<String, TopicSubscriber> subscribers = Collections.synchronizedMap(new HashMap<>());
    
    private final JMSService service;
    
    private IMessageCallback callback;
    
    public MessageConsumer(JMSService service) {
        this.service = service;
    }
    
    @Override
    public void setCallback(IMessageCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public boolean subscribe(String channel) {
        if (subscribers.get(channel) != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Already subscribed to Topic[%s]", channel));
            }
            return false;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("Subscribing to Topic[%s]", channel));
        }
        String selector = null; //JMSUtil.getMessageSelector(channel, getPublisherInfo());
        
        // This doesn't actually create a physical topic.  In ActiveMQ, a topic is created on-demand when someone with the
        // authority to create topics submits something to a topic.  By default, everyone has the authority to create topics.  See
        // http://markmail.org/message/us7v5ocnb65m4fdp#query:createtopic%20activemq%20jms+page:1+mid:tce6soq5g7rdkqnw+state:results --lrc
        Topic topic = service.createTopic(channel);
        TopicSubscriber subscriber = service.createSubscriber(topic, selector);
        
        try {
            subscriber.setMessageListener(new Subscriber(channel));
        } catch (JMSException e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        this.subscribers.put(channel, subscriber);
        return true;
    }
    
    @Override
    public boolean unsubscribe(String channel) {
        TopicSubscriber subscriber = this.subscribers.remove(channel);
        
        if (subscriber == null) {
            return false;
        }
        
        log.debug(String.format("Unsubscribing Subscriber[%s] for Topic [%s].", subscriber, channel));
        
        try {
            subscriber.setMessageListener(null);
            subscriber.close();
        } catch (JMSException e) {}
        
        return true;
    }
    
    /**
     * Reassert subscriptions.
     */
    public void assertSubscriptions() {
        for (String channel : subscribers.keySet()) {
            try {
                subscribers.put(channel, null);
                subscribe(channel);
            } catch (Throwable e) {
                break;
            }
        }
    }
    
    /**
     * Remove all remote subscriptions.
     */
    public void removeSubscriptions() {
        for (TopicSubscriber subscriber : subscribers.values()) {
            try {
                subscriber.close();
            } catch (Throwable e) {
                log.debug("Error closing subscriber", e);//is level appropriate - previously hidden exception -afranken
            }
        }
        
        subscribers.clear();
    }
    
}
