/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
     * @param tag
     * @param start
     * @param time
     * @param map
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
