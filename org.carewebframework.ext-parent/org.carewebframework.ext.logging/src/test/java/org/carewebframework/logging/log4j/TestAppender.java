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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple logging appender that allows inspection of logged messages.
 */
public class TestAppender extends AppenderSkeleton {
    
    private static List<String> messages = new ArrayList<String>();
    
    @Override
    protected void append(LoggingEvent event) {
        String message = layout == null ? event.getRenderedMessage() : layout.format(event);
        messages.add(message);
    }
    
    @Override
    public void close() {
        messages.clear();
    }
    
    @Override
    public boolean requiresLayout() {
        return false;
    }
    
    public static List<String> getMessages() {
        return messages;
    }
    
    public static void clear() {
        messages.clear();
    }
}
