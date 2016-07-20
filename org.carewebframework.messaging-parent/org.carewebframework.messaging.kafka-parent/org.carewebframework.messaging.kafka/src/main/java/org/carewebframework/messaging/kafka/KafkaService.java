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
    
    private final Producer<Object, Object> producer;
    
    private final Consumer<Object, Object> consumer;
    
    public KafkaService() {
        producer = new KafkaProducer<>(getConfigParams(ProducerConfig.class));
        consumer = new KafkaConsumer<>(getConfigParams(ConsumerConfig.class));
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
    public Producer<Object, Object> getProducer() {
        return producer;
    }
    
    public Consumer<Object, Object> getConsumer() {
        return consumer;
    }
    
    /**
     * A bit of a hack to return configuration parameters from the Spring property store as a map,
     * which is required to initialize Kafka consumers and producers. Uses reflection on the
     * specified class to enumerate static fields with a name ending in "_CONFIG". By Kafka
     * convention, these fields contain the names of configuration parameters.
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
                    String value = SpringUtil.getProperty("org.carewebframework.messaging.kafka." + key);
                    
                    if (value != null) {
                        params.put(key, value);
                    }
                    
                } catch (Exception e) {}
            }
        }
        
        return params;
    }
    
}
