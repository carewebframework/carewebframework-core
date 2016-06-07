package org.carewebframework.messaging.amqp.rabbitmq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.messaging.IMessageConsumer;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.thread.ThreadUtil;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * AMQP implementation of a message consumer.
 */
public class MessageConsumer implements IMessageConsumer {
    
    private class Subscriber extends SimpleMessageListenerContainer implements MessageListener {
        
        private final String channel;
        
        Subscriber(String channel) {
            this.channel = channel;
            setTaskExecutor(ThreadUtil.getTaskExecutor());
            setMessageListener(this);
            setConnectionFactory(broker.getConnectionFactory());
            setQueueNames(channel);
        }
        
        @Override
        public void onMessage(org.springframework.amqp.core.Message message) {
            Message msg = broker.convertMessage(channel, message);
            
            if (callback != null) {
                callback.onMessage(msg);
            }
        }
    }
    
    private final Map<String, Subscriber> subscribers = Collections.synchronizedMap(new HashMap<>());
    
    private final Broker broker;
    
    private IMessageCallback callback;
    
    public MessageConsumer(Broker broker) {
        this.broker = broker;
    }
    
    @Override
    public void setCallback(IMessageCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public boolean subscribe(String channel) {
        if (!subscribers.containsKey(channel)) {
            broker.ensureChannel(channel);
            Subscriber subscriber = new Subscriber(channel);
            subscriber.start();
            subscribers.put(channel, subscriber);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean unsubscribe(String channel) {
        if (subscribers.containsKey(channel)) {
            subscribers.remove(channel).stop();
            return true;
        }
        
        return false;
    }
    
}
