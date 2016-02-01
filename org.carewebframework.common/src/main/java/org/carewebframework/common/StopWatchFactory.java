/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.util.Map;

/**
 * Factory for creating stopwatch instances.
 */
public class StopWatchFactory {
    
    public interface IStopWatch {
        
        /**
         * Initializes the stopwatch with the specified tag and data.
         * 
         * @param tag Tag to identify the interval being timed.
         * @param data Arbitrary metadata.
         */
        void init(String tag, Map<String, Object> data);
        
        /**
         * Starts the stopwatch.
         */
        void start();
        
        /**
         * Stops the stopwatch.
         */
        void stop();
    }
    
    private static StopWatchFactory factory;
    
    private final Class<? extends IStopWatch> clazz;
    
    public static StopWatchFactory createFactory(Class<? extends IStopWatch> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Stopwatch class must not be null.");
        }
        
        if (factory != null) {
            throw new IllegalStateException("Stopwatch factory already initialized.");
        }
        
        return factory = new StopWatchFactory(clazz);
    }
    
    private StopWatchFactory(Class<? extends IStopWatch> clazz) {
        this.clazz = clazz;
    }
    
    /**
     * Returns an uninitialized stopwatch instance.
     * 
     * @return An uninitialized stopwatch instance. Will be null if the factory has not been
     *         initialized.
     */
    public static IStopWatch create() {
        if (factory == null) {
            throw new IllegalStateException("No stopwatch factory has been registered.");
        }
        
        try {
            return factory.clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create stopwatch instance.", e);
        }
    }
    
    /**
     * Returns a stopwatch instance initialized with the specified tag and data.
     * 
     * @param tag Tag to identify the interval being timed.
     * @param data Arbitrary metadata.
     * @return An initialized stopwatch instance. Will be null if the factory has not been
     *         initialized.
     */
    public static IStopWatch create(String tag, Map<String, Object> data) {
        IStopWatch sw = create();
        sw.init(tag, data);
        return sw;
    }
}
