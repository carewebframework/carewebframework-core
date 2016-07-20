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
package org.carewebframework.shell.plugins;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.zkoss.zk.ui.Execution;

/**
 * Thrown when an exception occurs during the execution of a plugin
 */
public class PluginExecutionException extends PluginException {
    
    private static final long serialVersionUID = 1L;
    
    private final Execution execution;
    
    /**
     * Provide additional contextual information regarding the exception
     * 
     * @param execution The execution context.
     * @return Formatted information from execution context.
     */
    private static String formatExecution(Execution execution) {
        StringBuffer buffer = new StringBuffer();
        if (execution != null) {
            HttpServletRequest req = (HttpServletRequest) execution.getNativeRequest();
            buffer.append("\nUserAgent: " + execution.getUserAgent());
            buffer.append("\nContextPath: " + execution.getContextPath());
            if (req != null) {
                HttpSession session = req.getSession();
                if (session != null) {
                    buffer.append("\nSession-----");
                    buffer.append("\n\tSession ID: " + session.getId());
                    buffer.append("\n\tCreationTime: " + new Date(session.getCreationTime()));
                    buffer.append("\n\tLastAccessedTime: " + new Date(session.getLastAccessedTime()));
                }
            }
        }
        return buffer.toString();
    }
    
    /**
     * Create a new PluginExecutionException
     * 
     * @param execution The ZK Execution (i.e. HttpServletRequest)
     * @param msg The message
     */
    public PluginExecutionException(Execution execution, String msg) {
        this(execution, msg, null);
    }
    
    /**
     * Create a new PluginExecutionException
     * 
     * @param execution The ZK Execution (i.e. HttpServletRequest)
     * @param msg The message
     * @param cause The Throwable
     */
    public PluginExecutionException(Execution execution, String msg, Throwable cause) {
        super(msg, cause, formatExecution(execution));
        this.execution = execution;
    }
    
    /**
     * Returns the ZK Execution (i.e. HttpServletRequest)
     * 
     * @return The ZK execution.
     */
    public Execution getExecution() {
        return this.execution;
    }
    
}
