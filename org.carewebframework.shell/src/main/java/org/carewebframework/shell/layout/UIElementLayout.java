/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import java.util.Collections;

import org.carewebframework.shell.plugins.PluginDefinition;

import org.springframework.util.StringUtils;

import org.zkoss.zul.Div;

/**
 * Wrapper for a UI layout component.
 */
public class UIElementLayout extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementLayout.class, UIElementBase.class);
        registerAllowedChildClass(UIElementLayout.class, UIElementBase.class);
    }
    
    private String layoutName;
    
    private boolean shared;
    
    private boolean linked = true;
    
    private UILayout layout;
    
    private final Div div = new Div();
    
    private PluginDefinition def;
    
    private boolean loaded;
    
    public UIElementLayout() {
        fullSize(div);
        setOuterComponent(div);
    }
    
    public UIElementLayout(String layoutName, boolean shared) {
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
    
    public UILayout getLayout() throws Exception {
        if (layout == null) {
            layout = new UILayout();
            String xml = LayoutUtil.getLayout(layoutName, shared);
            
            if (StringUtils.isEmpty(xml)) {
                raise("Unknown layout: " + layoutName);
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
    
    /**
     * If this is a linked layout, must deserialize from it.
     */
    @Override
    public void afterInitialize(boolean deserializing) throws Exception {
        super.afterInitialize(deserializing);
        
        if (linked) {
            internalDeserialize(false);
        }
    }
    
    /**
     * A linked layout has no serializable children.
     */
    @Override
    public Iterable<UIElementBase> getSerializableChildren() {
        return linked ? Collections.<UIElementBase> emptyList() : super.getSerializableChildren();
    }
    
    /**
     * Deserialize from the associated layout.
     * 
     * @param forced If true, force derserialization regardless of load state.
     */
    private void internalDeserialize(boolean forced) {
        if (!forced && loaded) {
            return;
        }
        
        if (loaded) {
            lockDescendants(false);
            removeChildren();
        }
        
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
            raise("Error loading layout.", e);
        }
    }
    
    /**
     * Checks for a circular reference to the same linked layout, throwing an exception if found.
     */
    private void checkForCircularReference() {
        UIElementLayout layout = this;
        
        while ((layout = layout.getAncestor(UIElementLayout.class)) != null) {
            if (layout.linked && layout.shared == shared && layout.layoutName.equals(layoutName)) {
                raise("Circular reference to layout " + layoutName);
            }
        }
    }
    
    /**
     * Sets the lock state for all descendants of this layout.
     * 
     * @param lock
     */
    private void lockDescendants(boolean lock) {
        lockDescendants(getChildren(), lock);
    }
    
    /**
     * Sets the lock state for all descendants of this layout.
     * 
     * @param children
     * @param lock
     */
    private void lockDescendants(Iterable<UIElementBase> children, boolean lock) {
        for (UIElementBase child : children) {
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
     * @return
     */
    public boolean getLinked() {
        return linked;
    }
    
    /**
     * Sets the linked state. A change in this state requires reloading of the associated layout.
     * 
     * @param linked
     */
    public void setLinked(boolean linked) {
        if (linked != this.linked) {
            this.linked = linked;
            internalDeserialize(true);
            getRoot().activate(true);
        }
    }
}
