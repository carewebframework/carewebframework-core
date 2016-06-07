package org.carewebframework.messaging.amqp.rabbitmq;

import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.api.messaging.Message;

public class MessageProducer implements IMessageProducer {
    
    private final Broker broker;
    
    public MessageProducer(Broker broker) {
        this.broker = broker;
    }
    
    @Override
    public boolean publish(Message message) {
        broker.sendMessage(message.getChannel(), message);
        return true;
    }
    
}
