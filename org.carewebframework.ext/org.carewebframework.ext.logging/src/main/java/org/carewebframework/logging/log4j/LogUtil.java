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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.NDC;
import org.carewebframework.ui.util.RequestUtil;

/**
 * <p>
 * <b>For internal use only (i.e. org.carewebframework.ui).</b>
 * <p>
 * Intentionally not using MDC as the map uses InheritableThreadLocal, which causes problems in a
 * web/pool environment. Using Log4j NDC %x and pushing the sessionId.
 * </p>
 * <b>Please take caution and ensure that add/remove calls are handled atomically</b> </p>
 */
public final class LogUtil {
    
    private static final Log log = LogFactory.getLog(LogUtil.class);
    
    private static final String LOG4J_SPECIFIC_CLASS = "org.apache.log4j.NDC";
    
    private static boolean log4jAware = true;
    
    static {
        try {
            Class.forName(LOG4J_SPECIFIC_CLASS);
        } catch (final Exception e) {
            log.warn("Not considering log4j diagnostic context due to missing log4j on classpath.");//Consider changing to debug
            log4jAware = false;
        }
    }
    
    /**
     * Returns whether this runtime is log4jAware.
     * 
     * @return true if instance is log4j aware
     */
    public static boolean isLog4jAware() {
        return log4jAware;
    }
    
    /**
     * Used to perform log implementation cleanup/shutdown. For example, log4j
     * LogManager.shutdown().
     */
    public static void shutdown() {
        if (log4jAware) {
            log.debug("Shutting down Log4J LogManager");
            LogManager.shutdown();
        }
    }
    
    /**
     * <p>
     * <b>Please take caution and ensure that add/remove calls are handled atomically</b>
     * </p>
     * <p>
     * As convenience, attempts to add the following diagnostic context to current thread.
     * <p>
     * 
     * @see RequestUtil#getStandardDiagnosticContext()
     */
    public static void addStandardDiagnosticContextToCurrentThread() {
        if (log4jAware) {
            addDiagnosticContextToCurrentThread(RequestUtil.getStandardDiagnosticContext());
        }
    }
    
    /**
     * <p>
     * <b>Please take caution and ensure that add/remove calls are handled atomically</b>
     * </p>
     * <p>
     * Add each String in collection to Diagnostic Context.
     * </p>
     * 
     * @param values - ordered list of strings to add as diagnostic context
     */
    public static void addDiagnosticContextToCurrentThread(final List<String> values) {
        if (log4jAware) {
            for (final String dc : values) {
                NDC.push(dc);
            }
        }
    }
    
    /**
     * Cleans up logging context, removing thread-specific values.
     */
    public static void removeDiagnosticContextFromCurrentThread() {
        if (log4jAware) {
            NDC.remove();
            NDC.clear();
        }
    }
}
