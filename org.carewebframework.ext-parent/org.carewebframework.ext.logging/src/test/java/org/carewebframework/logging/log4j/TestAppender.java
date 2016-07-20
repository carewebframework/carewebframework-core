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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple logging appender that allows inspection of logged messages.
 */
public class TestAppender extends AppenderSkeleton {
    
    private static List<String> messages = new ArrayList<>();
    
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
