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
package org.carewebframework.ui.util;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public final class MemoryLeakPreventionUtil {
    
    
    private static final Log log = LogFactory.getLog(MemoryLeakPreventionUtil.class);
    
    /**
     * Attempt all known cleanup measures.
     * 
     * @see #clearReferencesHttpClientKeepAliveThread()
     */
    /*@see #shutdownLogging()*/
    public static void clean() {
        log.info("Attempting to prevent known memory leaks");
        clearReferencesHttpClientKeepAliveThread();
        /*TODO Create some type of registry so that there is no dependency to the logging extension module.  shutdownLogging();*/
    }
    
    /**
     * @see LogUtil#shutdown()
     */
    /*    public static void shutdownLogging() {
            LogUtil.shutdown();
        }*/
    
    /**
     * CWI-1409
     * <p>
     * This does what Tomcat 7 context attribute clearReferencesHttpClientKeepAliveThread is
     * suggested to do but doesn't. Using reflection in this manner is a last resort.
     * <ul>
     * <li>JDK 7 is said to fix the keepAliveTimer thread bug.</li>
     * <li>Tomcat 7.0.x context attribute clearReferencesHttpClientKeepAliveThread is suggested to
     * fix this leak but does not</li>
     * </ul>
     */
    @SuppressWarnings("restriction")
    protected static void clearReferencesHttpClientKeepAliveThread() {
        try {
            Field kac = sun.net.www.http.HttpClient.class.getDeclaredField("kac");
            kac.setAccessible(true);
            Field keepAliveTimer = sun.net.www.http.KeepAliveCache.class.getDeclaredField("keepAliveTimer");
            keepAliveTimer.setAccessible(true);
            
            Thread t = (Thread) keepAliveTimer.get(kac.get(null));//kac is a static field, hence null argument
            
            if (t != null) {//May not actually be running
                log.debug("KeepAliveTimer contextClassLoader: " + t.getContextClassLoader());
                if (t.getContextClassLoader() == Thread.currentThread().getContextClassLoader()) {
                    t.setContextClassLoader(ClassLoader.getSystemClassLoader());
                    log.info("Changed KeepAliveTimer classloader to system to prevent leak.");
                }
            }
        } catch (Exception e) {
            log.warn(
                "Exception occurred attempting to prevent sun.net.www.http.HttpClient keepAliveTimer memory leak. Note: this code should not be necessary w/ java7",
                e);
        }
        
    }
}
