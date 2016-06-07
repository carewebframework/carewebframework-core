/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.messaging.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.api.messaging.Message;

public class MessageProducer implements IMessageProducer {
    
    private final Producer<Object, Object> producer;
    
    public MessageProducer(KafkaService service) {
        producer = service.getProducer();
    }
    
    @Override
    public boolean publish(Message message) {
        ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(message.getChannel(), message);
        producer.send(producerRecord);
        return false;
    }
    
}
