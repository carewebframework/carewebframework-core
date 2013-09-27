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

import java.util.regex.Pattern;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple logging filter that filters based on regular expressions to include or exclude log
 * messages.
 */
public class PatternMatchFilter extends Filter {
    
    private Pattern includePattern;
    
    private Pattern excludePattern;
    
    @Override
    public int decide(LoggingEvent event) {
        String message = event.getRenderedMessage();
        
        if (excludePattern != null && excludePattern.matcher(message).matches()) {
            return DENY;
        }
        
        if (includePattern != null && includePattern.matcher(message).matches()) {
            return ACCEPT;
        }
        
        return NEUTRAL;
    }
    
    public String getIncludePattern() {
        return includePattern == null ? null : includePattern.pattern();
    }
    
    public void setIncludePattern(String includePattern) {
        this.includePattern = includePattern == null ? null : Pattern.compile(includePattern);
    }
    
    public String getExcludePattern() {
        return excludePattern == null ? null : excludePattern.pattern();
    }
    
    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern == null ? null : Pattern.compile(excludePattern);
    }
    
}
