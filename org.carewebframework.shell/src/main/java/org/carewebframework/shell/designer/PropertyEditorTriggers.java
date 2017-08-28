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
package org.carewebframework.shell.designer;

import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementTrigger;
import org.carewebframework.shell.elements.ElementUI;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.fujion.component.BaseComponent;

/**
 * Property editor for managing triggers.
 */
public class PropertyEditorTriggers extends PropertyEditorCustomTree<ElementTrigger> {
    
    protected class TriggerProxy extends Proxy {

        public TriggerProxy(ElementTrigger child) {
            super(child);
        }
        
        public TriggerProxy(PluginDefinition def) {
            super(def);
        }
        
        @Override
        public ElementBase realize() {
            ElementBase real = realize(null);
            ((ElementUI) getTarget()).addTrigger((ElementTrigger) real);
            return real;
        }
        
    }

    public PropertyEditorTriggers() {
        super(ElementTrigger.class, null, false);
    }
    
    /**
     * Creates a new proxy for the specified child.
     *
     * @param child Element to be proxied. May not be null.
     * @return The proxy wrapping the specified child.
     */
    @Override
    protected Proxy newProxy(ElementBase child) {
        return addProxy(new TriggerProxy((ElementTrigger) child));
    }
    
    /**
     * Creates a new proxy for a child to be created.
     *
     * @param def Plugin definition for child to be created.
     * @return The proxy wrapping the specified plugin definition.
     */
    @Override
    protected Proxy newProxy(PluginDefinition def) {
        return addProxy(new TriggerProxy(def));
    }
    
    @Override
    protected void resequenceTargets(BaseComponent tc, ElementBase parent) {

    }
}
