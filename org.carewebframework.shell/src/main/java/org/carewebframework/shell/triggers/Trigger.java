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

import org.carewebframework.shell.elements.ElementBase;

/**
 * A trigger is a condition/action pair with zero or more plugin targets. When a condition is
 * triggered, the associated action is invoked on each target.
 */
public class Trigger extends ElementBase {

    static {
        registerAllowedChildClass(Trigger.class, TriggerCondition.class);
        registerAllowedChildClass(Trigger.class, TriggerAction.class);
    }

    private TriggerAction action;
    
    private TriggerCondition condition;

    private boolean executing;

    private final ITriggerCallback callback = () -> {
        onTrigger();
    };
    
    public Trigger() {
    }

    @Override
    protected void afterAddChild(ElementBase child) {
        super.afterAddChild(child);
        
        if (child instanceof TriggerAction) {
            action = (TriggerAction) child;
        } else if (child instanceof TriggerCondition) {
            condition = (TriggerCondition) child;
            condition.registerCallback(callback);
        }
    }

    @Override
    protected void afterRemoveChild(ElementBase child) {
        super.afterRemoveChild(child);
        
        if (action == child) {
            action = null;
        } else if (condition == child) {
            condition.unregisterCallback(callback);
            condition = null;
        }
    }

    private void onTrigger() {
        if (!executing && action != null) {
            try {
                executing = true;
                
                for (ElementBase target : getChildren()) {
                    if (target != action && target != condition) {
                        action.invokeAction(target);
                    }
                }
            } finally {
                executing = false;
            }
        }
    }
    
    @Override
    public String toString() {
        String cdx = condition == null ? "<not defined>" : condition.getDisplayName();
        String adx = action == null ? "<not defined" : action.getDisplayName();
        return "When: " + cdx + "\n\nThen: " + adx;
    }
    
}
