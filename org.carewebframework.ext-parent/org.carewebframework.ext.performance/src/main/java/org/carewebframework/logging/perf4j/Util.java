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
package org.carewebframework.logging.perf4j;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.StopWatchFactory.IStopWatch;

/**
 * Static utility methods.
 */
public class Util {
    
    public static final String STATUS_LOGGER = "org.carewebframework.logging.StatusLogger";
    
    private static final Log statusLogger = LogFactory.getLog(STATUS_LOGGER);
    
    private static final Log defaultLogger = LogFactory.getLog(IStopWatch.class);
    
    /**
     * Appends a field value to a string in perf4j format.
     * 
     * @param sb String builder containing string to receive field value.
     * @param field The name of the field.
     * @param value The value of the field.
     */
    private static void appendValue(StringBuilder sb, String field, Object value) {
        if (value != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            
            sb.append(field).append('[').append(value).append("]");
        }
    }
    
    /**
     * Formats timing information suitable for perf4j logging.
     * 
     * @param tag The tag.
     * @param start The start time.
     * @param time The current time.
     * @param map The message map.
     * @return Formatted timing info.
     */
    public static String formatForLogging(String tag, long start, long time, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        appendValue(sb, "tag", tag);
        appendValue(sb, "start", start);
        appendValue(sb, "time", time);
        appendValue(sb, "message", map);
        return sb.toString();
    }
    
    /**
     * Returns the target logger.
     * 
     * @param statusLogger If true, a logger that will display the performance information in the
     *            status display is returned. If false, the normal perf4j logger is returned.
     * @return The logger.
     */
    protected static Log getLogger(boolean statusLogger) {
        return statusLogger ? Util.statusLogger : Util.defaultLogger;
    }
    
    /**
     * Enforce static class.
     */
    private Util() {
    };
    
}
