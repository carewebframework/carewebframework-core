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
