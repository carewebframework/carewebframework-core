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

import java.text.DecimalFormat;

/**
 * Utility methods for managing numeric values.
 */
public class NumUtil {
    
    
    /**
     * Compares two integer values.
     * 
     * @param val1 First value.
     * @param val2 Second value.
     * @return &lt; 0 if val1 &lt; val2; 0 if val1 == val2; &gt; 0 if val1 &gt; val2
     */
    public static int compare(int val1, int val2) {
        return val1 - val2;
    }
    
    /**
     * Compares two double values.
     * 
     * @param val1 First value.
     * @param val2 Second value.
     * @return &lt; 0 if val1 &lt; val2; 0 if val1 == val2; &gt; 0 if val1 &gt; val2
     */
    public static int compare(double val1, double val2) {
        return Double.compare(val1, val2);
    }
    
    /**
     * Force an integer value to be within a specified range.
     * 
     * @param value Value to check
     * @param minValue Minimum allowable value
     * @param maxValue Maximum allowable value
     * @return The original value if within the specified range, or the lower or upper limit if
     *         outside the range.
     */
    public static int enforceRange(int value, int minValue, int maxValue) {
        return value < minValue ? minValue : (value > maxValue ? maxValue : value);
    }
    
    /**
     * Converts a double to a string without the trailing fractional zero. For example, 1.0 becomes
     * 1.
     * 
     * @param value Double value.
     * @return String equivalent.
     */
    public static String toString(double value) {
        return new DecimalFormat("0.#################").format(value);
    }
    
    /**
     * Enforce static class.
     */
    private NumUtil() {
    }
    
}
