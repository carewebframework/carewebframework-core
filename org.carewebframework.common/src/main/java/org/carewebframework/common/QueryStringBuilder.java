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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Builds a query string (without "?").
 */
public class QueryStringBuilder {
    
    private final StringBuilder sb = new StringBuilder();
    
    /**
     * Append a list of values. Each value is a separate name/value pair in the query string.
     * 
     * @param name Name of the query parameter.
     * @param values List of values for the query parameter.
     * @return Returns <code>this</code> for chaining.
     */
    public QueryStringBuilder append(String name, List<?> values) {
        if (values != null) {
            for (Object value : values) {
                append(name, value);
            }
        }
        
        return this;
    }
    
    /**
     * Appends one or more values. All are appended as a single name/value pair, with multiple
     * values separated by commas.
     * 
     * @param name Name of the query parameter.
     * @param values One or more values for the query parameter.
     * @return Returns <code>this</code> for chaining.
     */
    public QueryStringBuilder append(String name, Object... values) {
        if (values != null && values.length > 0) {
            boolean first = true;
            
            for (Object value : values) {
                if (value == null) {
                    continue;
                }
                
                if (first) {
                    first = false;
                    
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    
                    sb.append(encode(name)).append('=');
                } else {
                    sb.append(',');
                }
                
                sb.append(encode(value));
            }
        }
        
        return this;
    }
    
    /**
     * Encode reserved characters.
     * 
     * @param value Value to be encoded.
     * @return Encoded value.
     */
    private String encode(Object value) {
        try {
            return URLEncoder.encode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    /**
     * Returns the length of the query string.
     * 
     * @return Length of the query string.
     */
    public int length() {
        return sb.length();
    }
    
    /**
     * Removes all content.
     */
    public void clear() {
        sb.delete(0, sb.length());
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
}
