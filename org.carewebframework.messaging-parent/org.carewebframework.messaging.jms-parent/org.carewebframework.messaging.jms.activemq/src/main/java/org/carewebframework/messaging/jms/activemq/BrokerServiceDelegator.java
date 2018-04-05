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
package org.carewebframework.messaging.jms.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Delegates calls to underlying broker service.
 */
public class BrokerServiceDelegator {
    
    private static final String SERIALIZABLE_PACKAGES_PROP = "org.apache.activemq.SERIALIZABLE_PACKAGES";
    
    private static final Log log = LogFactory.getLog(BrokerServiceDelegator.class);
    
    static {
        System.setProperty(SERIALIZABLE_PACKAGES_PROP, "*");
    }
    
    private final BrokerService brokerService;
    
    public BrokerServiceDelegator(BrokerService brokerService) {
        this.brokerService = brokerService;
    }
    
    /**
     * Start BrokerService
     *
     * @throws Exception when problem occurs starting {@linkplain #brokerService}
     */
    public void start() throws Exception {
        log.info("Starting BrokerService");
        this.brokerService.start();
    }
    
    /**
     * Stop BrokerService
     *
     * @throws Exception when problem occurs starting {@linkplain #brokerService}
     */
    public void stop() throws Exception {
        log.info("Stopping BrokerService");
        this.brokerService.stop();
    }
    
    /**
     * @return the brokerService
     */
    public BrokerService getBrokerService() {
        return this.brokerService;
    }
    
}
