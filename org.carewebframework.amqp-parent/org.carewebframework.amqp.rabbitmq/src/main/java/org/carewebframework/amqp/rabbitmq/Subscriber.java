/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.amqp.rabbitmq;

import java.util.Collection;

import com.rabbitmq.client.Channel;

import org.carewebframework.api.event.PublisherInfo;
import org.carewebframework.api.thread.ThreadUtil;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Subscriber is really an AMQP message listener that has an additional delivery filter based on
 * recipients possibly specified in the message header.
 */
public class Subscriber extends SimpleMessageListenerContainer {
    
    private final Collection<String> recipientIds;
    
    public Subscriber(PublisherInfo publisherInfo) {
        this.recipientIds = publisherInfo.getAttributes().values();
        setTaskExecutor(ThreadUtil.getTaskExecutor());
    }
    
    @Override
    protected void invokeListener(Channel channel, Message message) throws Exception {
        MessageProperties props = message.getMessageProperties();
        String recipients = props == null ? null : (String) props.getHeaders().get(Broker.RECIPIENTS_PROPERTY);
        boolean found = true;
        
        if (recipients != null) {
            for (String recipient : recipients.split("\\,")) {
                found = recipientIds.contains(recipient);
                
                if (found) {
                    break;
                }
            }
        }
        
        if (found) {
            super.invokeListener(channel, message);
        }
    }
}
