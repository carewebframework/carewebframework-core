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

import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.api.messaging.Message;

public class MessageProducer implements IMessageProducer {
    
    private final JMSService service;
    
    public MessageProducer(JMSService service) {
        this.service = service;
    }
    
    @Override
    public boolean publish(Message message) {
        javax.jms.Message msg = service.createObjectMessage(message.getChannel(), message, null, null);
        service.sendMessage(message.getChannel(), msg);
        return true;
    }
    
}
