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
package org.carewebframework.shell.triggers;

import java.util.HashSet;
import java.util.Set;

import org.carewebframework.shell.elements.ElementBase;

/**
 * Conditional logic for a trigger.
 */
public abstract class TriggerCondition extends ElementBase implements ITriggerCondition {
    
    static {
        registerAllowedParentClass(TriggerCondition.class, Trigger.class);
    }

    private final Set<ITriggerCallback> callbacks = new HashSet<>();

    public TriggerCondition() {
        init();
    }
    
    @Override
    public void registerCallback(ITriggerCallback callback) {
        callbacks.add(callback);
    }
    
    @Override
    public void unregisterCallback(ITriggerCallback callback) {
        callbacks.remove(callback);
    }
    
    protected void invokeCallbacks() {
        for (ITriggerCallback callback : callbacks) {
            callback.onTrigger();
        }
    }
    
    protected abstract void init();
}
