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

import org.carewebframework.api.FrameworkRuntimeException;

/**
 * Superclass for all exceptions thrown due to a plugin-related error.
 * <p>
 * Note that this is a runtime (unchecked) exception. Plugin exceptions are usually fatal; there is
 * no reason for them to be checked.
 */
public class PluginException extends FrameworkRuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new PluginsException with the specified message.
     * 
     * @param msg the detail message
     */
    public PluginException(String msg) {
        super(msg);
    }
    
    /**
     * Create a new PluginsException with the specified message and root cause.
     * 
     * @param msg the detail message
     * @param cause the root cause
     * @param throwableContext Additional context info
     * @param args Additional arguments
     */
    public PluginException(String msg, Throwable cause, String throwableContext, Object... args) {
        super(msg, cause, throwableContext, args);
    }
    
}
