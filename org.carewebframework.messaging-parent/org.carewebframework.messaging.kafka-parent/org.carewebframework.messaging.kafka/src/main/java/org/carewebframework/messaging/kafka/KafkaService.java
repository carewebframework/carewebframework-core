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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.carewebframework.api.spring.SpringUtil;

/**
 * Delegates calls to underlying producer service.
 */
public class KafkaService {
    
    private static final Log log = LogFactory.getLog(KafkaService.class);
    
    private final Producer<Object, Message> producer;
    
    private final Map<String, Object> consumerConfig;
    
    public KafkaService() {
        consumerConfig = getConfigParams(ConsumerConfig.class);
        producer = new KafkaProducer<Object, Message>(getConfigParams(ProducerConfig.class));
    }
    
    /**
     * Start Services
     * 
     * @throws Exception when problem occurs starting {@linkplain #brokerService}
     */
    public void start() throws Exception {
        log.info("Starting Kafka producer service");
    }
    
    /**
     * Stop BrokerService
     * 
     * @throws Exception when problem occurs starting {@linkplain #brokerService}
     */
    public void stop() throws Exception {
        log.info("Stopping Kafka services");
        producer.close();
    }
    
    /**
     * @return The producer
     */
    public Producer<Object, Message> getProducer() {
        return producer;
    }
    
    public Consumer<Object, Message> getNewConsumer() {
        return new KafkaConsumer<>(consumerConfig);
    }
    
    /**
     * A bit of a hack to return possible configuration parameters from Spring property store.
     * 
     * @param clazz Class defining configuration parameters as static fields.
     * @return A map of configuration parameters with their values from the Spring property store.
     */
    private Map<String, Object> getConfigParams(Class<?> clazz) {
        Map<String, Object> params = new HashMap<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().endsWith("_CONFIG")) {
                try {
                    String key = field.get(null).toString();
                    String value = SpringUtil.getProperty("org.carewebframework.kafka." + key);
                    
                    if (value != null) {
                        params.put(key, value);
                    }
                    
                } catch (Exception e) {}
            }
        }
        
        return params;
    }
    
}
