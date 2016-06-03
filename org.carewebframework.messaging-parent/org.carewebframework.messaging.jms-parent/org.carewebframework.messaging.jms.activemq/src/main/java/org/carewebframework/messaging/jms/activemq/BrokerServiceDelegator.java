/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.messaging.jms.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Delegates calls to underlying broker service.
 */
public class BrokerServiceDelegator {
    
    private static final Log log = LogFactory.getLog(BrokerServiceDelegator.class);
    
    private BrokerService brokerService;
    
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
    
    /**
     * @param brokerService the brokerService to set
     */
    public void setBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }
    
}
