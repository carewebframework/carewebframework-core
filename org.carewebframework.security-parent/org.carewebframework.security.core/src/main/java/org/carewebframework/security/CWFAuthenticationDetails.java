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
package org.carewebframework.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * Extends the stock Spring web authentication details class by adding the ability to add arbitrary
 * detail objects to it.
 */
public class CWFAuthenticationDetails extends WebAuthenticationDetails {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(CWFAuthenticationDetails.class);
    
    public static final String ATTR_USER = "user";
    
    private final Map<String, Object> details = new HashMap<>();
    
    public CWFAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }
    
    /**
     * Sets the specified detail element to the specified value.
     * 
     * @param name Name of the detail element.
     * @param value Value for the detail element. A null value removes any existing detail element.
     */
    public void setDetail(String name, Object value) {
        if (value == null) {
            details.remove(name);
        } else {
            details.put(name, value);
        }
        
        if (log.isDebugEnabled()) {
            if (value == null) {
                log.debug("Detail removed: " + name);
            } else {
                log.debug("Detail added: " + name + " = " + value);
            }
        }
    }
    
    /**
     * Returns the specified detail element.
     * 
     * @param name Name of the detail element.
     * @return Value of the detail element, or null if not found.
     */
    public Object getDetail(String name) {
        return name == null ? null : details.get(name);
    }
}
