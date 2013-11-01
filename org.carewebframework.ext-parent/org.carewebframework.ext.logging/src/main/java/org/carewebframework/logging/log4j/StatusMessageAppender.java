/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.logging.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;

/**
 * Simple custom appender that sends a log message to the status window via a STATUS event.
 */
public class StatusMessageAppender extends AppenderSkeleton {
    
    @Override
    public void close() {
    }
    
    @Override
    public boolean requiresLayout() {
        return true;
    }
    
    /**
     * Sends to formatted log message as a STATUS event.
     */
    @Override
    protected void append(LoggingEvent event) {
        IEventManager eventManager = EventManager.getInstance();
        
        if (eventManager != null) {
            String message = layout == null ? event.getRenderedMessage() : layout.format(event);
            eventManager.fireLocalEvent("STATUS.TIMING", message);
        }
    }
    
}
