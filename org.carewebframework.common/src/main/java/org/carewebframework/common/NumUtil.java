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
     * @return -1 if val1 < val2; 0 if val1 == val2; 1 if val1 > val2
     */
    public static int compare(int val1, int val2) {
        return val1 - val2;
    }
    
    /**
     * Compares two double values.
     * 
     * @param val1 First value.
     * @param val2 Second value.
     * @return <0 if val1 < val2; 0 if val1 == val2; >0 if val1 > val2
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
     * Converts a double to a string without the trailing fractional zero.
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
    };
    
}
