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
