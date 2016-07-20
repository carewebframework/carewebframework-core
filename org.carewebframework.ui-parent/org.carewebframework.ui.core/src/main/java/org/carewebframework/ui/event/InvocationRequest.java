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
