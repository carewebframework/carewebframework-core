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

import org.zkoss.zk.ui.Execution;

/**
 * Represents a plugin lifecycle exception.
 */
public class PluginLifecycleEventException extends PluginExecutionException {
    
    private static final long serialVersionUID = 1L;
    
    public PluginLifecycleEventException(Execution execution, String msg) {
        super(execution, msg);
    }
    
    public PluginLifecycleEventException(Execution execution, String msg, Throwable cause) {
        super(execution, msg, cause);
    }
    
}
