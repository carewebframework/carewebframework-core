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

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This subclasses the log4j pattern parser by adding the following additional converters that
 * return field values from the perf4j log message:
 * <UL>
 * <LI><B>B -</B> Beginning time ('start' field)
 * <LI><B>E -</B> Elapsed time ('time' field)
 * <LI><B>G -</B> Message field ('message' field)
 * <LI><B>T -</B> Tag ('tag' field)
 * </UL>
 */
public class Perf4JPatternParser extends PatternParser {
    
    /**
     * Pattern converter that returns the value of the associated field.
     */
    private static class BasicPatternConverter extends PatternConverter {
        
        final String field;
        
        BasicPatternConverter(FormattingInfo formattingInfo, String field) {
            super(formattingInfo);
            this.field = field;
        }
        
        /**
         * Returns the field value from the log event message.
         */
        @Override
        public String convert(LoggingEvent event) {
            return extract(event.getRenderedMessage(), field);
        }
        
        /**
         * Extracts the value for the specified field from the perf4j log message.
         * 
         * @param message The perf4j log message.
         * @param field The name of the field whose value is sought.
         * @return The value of the specified field, or null if not found.
         */
        private String extract(String message, String field) {
            if (field == null || field.isEmpty()) {
                return "";
            }
            
            String subfield = null;
            
            if (field.contains(".")) {
                String pcs[] = field.split("\\.", 2);
                field = pcs[0];
                subfield = pcs[1];
            }
            
            int i = message.indexOf(field + "[");
            
            if (i >= 0) {
                i += field.length() + 1;
                int j = message.indexOf(']', i);
                
                if (j > 0) {
                    String value = message.substring(i, j);
                    
                    if (subfield != null && !subfield.isEmpty()) {
                        Object val = fromMessage(value).get(subfield);
                        value = val == null ? "" : val.toString();
                    }
                    
                    return value;
                }
            }
            
            return "";
        }
        
        private Map<String, Object> fromMessage(String message) {
            Map<String, Object> messageValues = new TreeMap<>();
            
            if (message != null && message.matches("\\{.*\\}")) {
                int len = message.length();
                String[] entries = message.substring(1, len - 1).split(",");
                
                for (String entry : entries) {
                    String[] kv = entry.trim().split("=");
                    
                    if (kv.length == 2) {
                        messageValues.put(kv[0], kv[1]);
                    }
                }
            }
            return messageValues;
        }
        
    }
    
    public Perf4JPatternParser(String pattern) {
        super(pattern);
    }
    
    /**
     * Adds additional converters. Delegates to the super method if not one of the specialized
     * converters.
     */
    @Override
    protected void finalizeConverter(char c) {
        if (c != 'P') {
            super.finalizeConverter(c);
            return;
        }
        
        PatternConverter pc = new BasicPatternConverter(this.formattingInfo, extractOption());
        currentLiteral.setLength(0);
        addConverter(pc);
    }
    
}
