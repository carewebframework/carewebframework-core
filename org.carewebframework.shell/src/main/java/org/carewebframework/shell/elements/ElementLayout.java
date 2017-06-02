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

import org.carewebframework.shell.ancillary.UIException;
import org.carewebframework.shell.layout.LayoutIdentifier;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.shell.layout.Layout;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.web.component.Div;
import org.springframework.util.StringUtils;

/**
 * Wrapper for a UI layout component.
 */
public class ElementLayout extends ElementBase {
    
    static {
        registerAllowedParentClass(ElementLayout.class, ElementBase.class);
        registerAllowedChildClass(ElementLayout.class, ElementBase.class);
    }
    
    private String layoutName;
    
    private boolean shared;
    
    private boolean linked = true;
    
    private Layout layout;
    
    private final Div div = new Div();
    
    private PluginDefinition def;
    
    private boolean loaded;
    
    private boolean initializing;
    
    public ElementLayout() {
        fullSize(div);
        setOuterComponent(div);
    }
    
    public ElementLayout(String layoutName, boolean shared) {
        this();
        this.layoutName = layoutName;
        this.shared = shared;
        def = new PluginDefinition(PluginDefinition.getDefinition(getClass()));
        super.setDefinition(def);
        initDefinition();
    }
    
    private void initDefinition() {
        if (def != null) {
            def.setName(layoutName);
            def.setCategory("Layouts\\" + (shared ? "Shared" : "Private"));
        }
    }
    
    @Override
    public void setDefinition(PluginDefinition definition) {
        if (definition != null && def == null) {
            def = new PluginDefinition(definition);
            layoutName = def.getName();
            String category = def.getCategory();
            shared = category != null && category.contains("Shared");
            super.setDefinition(def);
        }
    }
    
    @Override
    public String getDisplayName() {
        return (linked ? "Linked" : "Embedded") + " Layout - " + layoutName;
    }
    
    public Layout getLayout() throws Exception {
        if (layout == null) {
            layout = new Layout();
            String xml = LayoutUtil.getLayoutContent(new LayoutIdentifier(layoutName, shared));
            
            if (StringUtils.isEmpty(xml)) {
                UIException.raise("Unknown layout: " + layoutName);
            }
            
            layout.loadFromText(xml);
        }
        
        return layout;
    }
    
    public String getLayoutName() {
        return layoutName;
    }
    
    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
        initDefinition();
    }
    
    public boolean getShared() {
        return shared;
    }
    
    public void setShared(boolean shared) {
        this.shared = shared;
        initDefinition();
    }
    
    @Override
    public void beforeInitialize(boolean deserializing) throws Exception {
        initializing = true;
        super.beforeInitialize(deserializing);
    }
    
    /**
     * If this is a linked layout, must deserialize from it.
     */
    @Override
    public void afterInitialize(boolean deserializing) throws Exception {
        super.afterInitialize(deserializing);
        
        if (linked) {
            internalDeserialize(false);
        }
        
        initializing = false;
    }
    
    /**
     * A linked layout has no serializable children.
     */
    @Override
    public Iterable<ElementBase> getSerializableChildren() {
        return linked ? Collections.<ElementBase> emptyList() : super.getSerializableChildren();
    }
    
    /**
     * Deserialize from the associated layout.
     * 
     * @param forced If true, force deserialization regardless of load state.
     */
    private void internalDeserialize(boolean forced) {
        if (!forced && loaded) {
            return;
        }
        
        lockDescendants(false);
        removeChildren();
        loaded = true;
        
        try {
            if (linked) {
                checkForCircularReference();
            }
            
            getLayout().deserialize(this);
            
            if (linked) {
                lockDescendants(true);
            }
        } catch (Exception e) {
            UIException.raise("Error loading layout.", e);
        }
    }
    
    /**
     * Checks for a circular reference to the same linked layout, throwing an exception if found.
     */
    private void checkForCircularReference() {
        ElementLayout layout = this;
        
        while ((layout = layout.getAncestor(ElementLayout.class)) != null) {
            if (layout.linked && layout.shared == shared && layout.layoutName.equals(layoutName)) {
                UIException.raise("Circular reference to layout " + layoutName);
            }
        }
    }
    
    /**
     * Sets the lock state for all descendants of this layout.
     * 
     * @param lock The lock state.
     */
    private void lockDescendants(boolean lock) {
        lockDescendants(getChildren(), lock);
    }
    
    /**
     * Sets the lock state for all descendants of this layout.
     * 
     * @param children List of descendants.
     * @param lock The lock state.
     */
    private void lockDescendants(Iterable<ElementBase> children, boolean lock) {
        for (ElementBase child : children) {
            child.setLocked(lock);
            lockDescendants(child.getChildren(), lock);
        }
    }
    
    /**
     * Returns the linked state. If true, the associated layout is linked by reference. Any changes
     * to the layout will be reflected when the containing layout is loaded. If false, the
     * associated layout is embedded. Any changes to the original layout will not be reflected when
     * the containing layout is loaded. Additionally, all child UI elements of a linked layout are
     * locked and may not be modified. Child elements of an embedded layout may be freely edited.
     * 
     * @return The linked state.
     */
    public boolean getLinked() {
        return linked;
    }
    
    /**
     * Sets the linked state. A change in this state requires reloading of the associated layout.
     * 
     * @param linked The linked state.
     */
    public void setLinked(boolean linked) {
        if (linked != this.linked) {
            this.linked = linked;
            
            if (!initializing) {
                internalDeserialize(true);
                getRoot().activate(true);
            }
        }
    }
}
