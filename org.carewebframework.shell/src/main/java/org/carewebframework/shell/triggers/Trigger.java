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
 * A trigger is a condition/action pair with zero or more plugin targets. When a condition is
 * triggered, the associated action is invoked on each target.
 */
public class Trigger implements ITriggerCallback {

    private final Set<ElementBase> targets = new HashSet<>();

    private final ITriggerAction action;
    
    private final ITriggerCondition condition;

    private boolean executing;

    public Trigger(ITriggerCondition condition, ITriggerAction action) {
        this.action = action;
        this.condition = condition;
        condition.registerCallback(this);
    }

    public boolean addTarget(ElementBase target) {
        return targets.add(target);
    }

    public boolean removeTarget(ElementBase target) {
        return targets.remove(target);
    }

    @Override
    public void onTrigger() {
        if (!executing) {
            try {
                executing = true;
                
                for (ElementBase target : targets) {
                    action.invokeAction(target);
                }
            } finally {
                executing = false;
            }
        }
    }
    
    public String getDescription() {
        return "When: " + condition.getDescription() + "\n\nThen: " + action.getDescription();
    }
    
}
