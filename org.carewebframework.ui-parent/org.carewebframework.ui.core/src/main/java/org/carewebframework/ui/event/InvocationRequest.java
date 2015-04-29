/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.event;

import org.zkoss.zk.ui.event.Event;

/**
 * A single invocation request that will be sent to the target.
 */
public class InvocationRequest extends Event {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a help request.
     * 
     * @param methodName Name of the method to invoke on the target.
     * @param args Arguments to be passed to the method.
     */
    protected InvocationRequest(String methodName, Object... args) {
        super(methodName, null, args);
    }
    
    /**
     * Returns method arguments.
     * 
     * @return Method arguments.
     */
    public Object[] getArgs() {
        return (Object[]) getData();
    }
}
