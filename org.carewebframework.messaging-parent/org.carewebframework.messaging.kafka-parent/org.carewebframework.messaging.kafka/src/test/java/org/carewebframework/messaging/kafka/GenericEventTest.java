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

import org.carewebframework.api.event.IGlobalEventDispatcher;

/**
 * Perform remote event testing.
 */
public class GenericEventTest extends org.carewebframework.api.test.GenericEventTest {
    
    public GenericEventTest() {
        super();
        this.remote = true;
        IGlobalEventDispatcher ged = appContext.getBean("globalEventDispatcher", IGlobalEventDispatcher.class);
        this.recipientId = ged.getPublisherInfo().getEndpointId();
    }
}
