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

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * Subclasses log4j's PatternLayout class to use the extended pattern parser.
 */
public class Perf4JPatternLayout extends PatternLayout {
    
    /**
     * Returns the extended pattern parser.
     */
    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new Perf4JPatternParser(pattern);
    }
    
}
