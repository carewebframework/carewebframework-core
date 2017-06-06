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
package org.carewebframework.shell.layout;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;

/**
 * Represents a UI element occurrence within a layout.
 */
public class LayoutElement extends LayoutNode {
    
    /**
     * Represents the root node for a layout.
     */
    public static class LayoutRoot extends LayoutElement {

        public LayoutRoot() {
            super();
        }
    }
    
    private final LayoutElement parent;

    private final PluginDefinition pluginDefinition;
    
    private final List<LayoutElement> elements = new ArrayList<>();
    
    private final List<LayoutTrigger> triggers = new ArrayList<>();
    
    private LayoutElement() {
        super("layout");
        this.parent = null;
        this.pluginDefinition = PluginRegistry.getInstance().get("_desktop");
    }

    public LayoutElement(PluginDefinition pluginDefinition, LayoutElement parent) {
        super("element");
        this.parent = parent;
        this.pluginDefinition = pluginDefinition;
        
        if (parent != null) {
            parent.getElements().add(this);
        }
    }
    
    protected List<LayoutElement> getElements() {
        return elements;
    }
    
    protected List<LayoutTrigger> getTriggers() {
        return triggers;
    }
    
    protected PluginDefinition getDefinition() {
        return pluginDefinition;
    }

    protected LayoutElement getParent() {
        return parent;
    }
    
    protected LayoutElement getNextSibling() {
        int i = parent == null ? 0 : parent.elements.indexOf(this) + 1;
        return i == 0 || i >= parent.elements.size() ? null : parent.elements.get(i);
    }
    
}
