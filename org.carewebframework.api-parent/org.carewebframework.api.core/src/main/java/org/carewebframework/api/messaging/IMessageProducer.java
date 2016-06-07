/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.messaging;

/**
 * Interface to be implemented by every message producer.
 */
public interface IMessageProducer {
    
    /**
     * Publish a message.
     * 
     * @param message The message to publish.
     * @return True if successfully published.
     */
    boolean publish(Message message);
    
}
