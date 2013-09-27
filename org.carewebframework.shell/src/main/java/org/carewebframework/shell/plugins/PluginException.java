/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.carewebframework.api.FrameworkRuntimeException;

/**
 * Abstract superclass for all exceptions thrown due to a plugin-related error.
 * <p>
 * Note that this is a runtime (unchecked) exception. Plugin exceptions are usually fatal; there is
 * no reason for them to be checked.
 */
public abstract class PluginException extends FrameworkRuntimeException {
    
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
