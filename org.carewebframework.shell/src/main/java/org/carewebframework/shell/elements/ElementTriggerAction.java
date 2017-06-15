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

import org.carewebframework.shell.triggers.ITriggerAction;

/**
 * Base class trigger-based actions.
 */
public abstract class ElementTriggerAction extends ElementBase implements ITriggerAction {

    static {
        registerAllowedParentClass(ElementTriggerAction.class, ElementTrigger.class);
    }
    
    protected abstract boolean doInvokeAction(ElementUI target);
    
    protected ElementTrigger getTrigger() {
        return (ElementTrigger) getParent();
    }
    
    @Override
    public final boolean invokeAction(ElementUI target) {
        if (isEnabled() && !isDesignMode()) {
            return doInvokeAction(target);
        }

        return true;
    }
    
}
