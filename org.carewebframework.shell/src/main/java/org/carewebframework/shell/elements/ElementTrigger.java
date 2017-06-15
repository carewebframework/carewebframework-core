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
package org.carewebframework.shell.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carewebframework.shell.plugins.PluginException;
import org.carewebframework.shell.triggers.ITriggerCallback;

/**
 * A trigger is a condition/action pair with zero or more plugin targets. When a condition is
 * triggered, the associated action is invoked on each target.
 */
public class ElementTrigger extends ElementBase {

    static {
        registerAllowedChildClass(ElementTrigger.class, ElementTriggerCondition.class, 1);
        registerAllowedChildClass(ElementTrigger.class, ElementTriggerAction.class, 1);
    }

    private ElementTriggerAction action;
    
    private ElementTriggerCondition condition;

    private boolean executing;

    private final Set<ElementUI> targets = new HashSet<>();

    private final ITriggerCallback callback = () -> {
        onTrigger();
    };
    
    public ElementTrigger() {
    }

    @Override
    protected void beforeAddChild(ElementBase child) {
        super.beforeAddChild(child);
        
        if (child instanceof ElementTriggerAction) {
            if (action != null && action != child) {
                throw new PluginException("This trigger already has an action.");
            }
        } else if (child instanceof ElementTriggerCondition) {
            if (condition != null && condition != child) {
                throw new PluginException("This trigger already has a condition.");
            }
        }
    }

    @Override
    protected void afterAddChild(ElementBase child) {
        super.afterAddChild(child);
        
        if (child instanceof ElementTriggerAction) {
            action = (ElementTriggerAction) child;
        } else if (child instanceof ElementTriggerCondition) {
            condition = (ElementTriggerCondition) child;
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
        if (!executing && action != null && !targets.isEmpty() && isEnabled() && !isDesignMode()) {
            try {
                executing = true;
                
                for (ElementUI target : targets) {
                    action.invokeAction(target);
                }
            } finally {
                executing = false;
            }
        }
    }
    
    public ElementTriggerCondition getCondition() {
        return condition;
    }

    public ElementTriggerAction getAction() {
        return action;
    }

    public void addTarget(ElementUI target) {
        targets.add(target);
    }
    
    public void removeTarget(ElementUI target) {
        targets.remove(target);
    }
    
    public Set<ElementUI> getTargets() {
        return Collections.unmodifiableSet(targets);
    }
    
    @Override
    public String toString() {
        String cdx = condition == null ? "<not defined>" : condition.getDisplayName();
        String adx = action == null ? "<not defined" : action.getDisplayName();
        return "When: " + cdx + "\n\nThen: " + adx;
    }
    
}
