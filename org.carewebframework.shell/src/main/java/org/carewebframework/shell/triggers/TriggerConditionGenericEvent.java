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

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.shell.elements.ElementTriggerCondition;

/**
 * Condition that triggers based on a generic event.
 */
public class TriggerConditionGenericEvent extends ElementTriggerCondition {
    
    private final String eventName;
    
    private boolean subscribed;

    private final IGenericEvent<Object> eventListener = (eventName, eventObject) -> {
        if (!excludeEvent(eventObject)) {
            invokeCallbacks();
        }
    };

    public TriggerConditionGenericEvent(String eventName) {
        this.eventName = eventName;
    }

    protected boolean excludeEvent(Object eventObject) {
        return false;
    }

    @Override
    public void registerCallback(ITriggerCallback callback) {
        super.registerCallback(callback);
        updateSubscription();
    }

    @Override
    public void unregisterCallback(ITriggerCallback callback) {
        super.unregisterCallback(callback);
        updateSubscription();
    }

    private void updateSubscription() {
        boolean hasCallbacks = getCallbacks().size() > 0;
        
        if (hasCallbacks != subscribed) {
            if (hasCallbacks) {
                EventManager.getInstance().subscribe(eventName, eventListener);
            } else {
                EventManager.getInstance().unsubscribe(eventName, eventListener);

            }
            
            subscribed = hasCallbacks;
        }
    }

}
