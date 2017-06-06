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

/**
 * Base class for rules governing plugin behavior.
 */
public abstract class TriggerCondition extends TriggerComponent implements ITriggerCondition {
    
    private final Set<ITriggerCallback> callbacks = new HashSet<>();

    protected TriggerCondition(String id, String description) {
        super(id, description);
    }
    
    @Override
    public void registerCallback(ITriggerCallback callback) {
        callbacks.add(callback);
    }
    
    protected void invokeCallbacks() {
        for (ITriggerCallback callback : callbacks) {
            callback.onTrigger();
        }
    }
    
}
