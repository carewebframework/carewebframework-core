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
            throw new IllegalStateException("Stopwatch factory already registered.");
        }
        
        return factory = new StopWatchFactory(clazz);
    }
    
    private StopWatchFactory(Class<? extends IStopWatch> clazz) {
        this.clazz = clazz;
    }
    
    /**
     * Returns true if a factory has been registered.
     * 
     * @return True if a factory has been registered.
     */
    public static boolean hasFactory() {
        return factory != null;
    }
    
    /**
     * Returns an uninitialized stopwatch instance.
     * 
     * @return An uninitialized stopwatch instance. Will be null if the factory has not been
     *         initialized.
     */
    public static IStopWatch create() {
        if (factory == null) {
            throw new IllegalStateException("No stopwatch factory registered.");
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
