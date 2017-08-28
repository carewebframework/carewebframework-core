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

import org.carewebframework.shell.designer.PropertyEditorOrderedChildren;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.action.ActionUtil;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Toolbar;
import org.fujion.component.Toolbar.Alignment;

/**
 * Implements a shared toolbar.
 */
public class ElementToolbar extends ElementUI {
    
    static {
        registerAllowedChildClass(ElementToolbar.class, ElementUI.class, Integer.MAX_VALUE);
        registerAllowedParentClass(ElementToolbar.class, ElementUI.class);
        PropertyTypeRegistry.register("children", PropertyEditorOrderedChildren.class);
    }
    
    private final Toolbar toolbar;
    
    public ElementToolbar() {
        this(new Toolbar());
    }
    
    public ElementToolbar(Toolbar toolbar) {
        super();
        this.toolbar = toolbar;
        toolbar.setAlignment(Toolbar.Alignment.END);
        toolbar.addClass("cwf-desktop-toolbar btn-toolbar");
        setOuterComponent(toolbar);
    }
    
    public Toolbar getToolbar() {
        return toolbar;
    }
    
    /**
     * Adds a component to the toolbar.
     *
     * @param component Component to add. If the component is a toolbar itself, its children will be
     *            added to the toolbar.
     * @param action The action to associate with the component.
     */
    public void addToolbarComponent(BaseComponent component, String action) {
        BaseComponent ref = toolbar.getFirstChild();
        
        if (component instanceof Toolbar) {
            BaseComponent child;
            
            while ((child = component.getFirstChild()) != null) {
                toolbar.addChild(child, ref);
            }
            
        } else {
            toolbar.addChild(component, ref);
            ActionUtil.addAction(component, action);
        }
    }
    
    @Override
    protected void beforeAddChild(ElementBase child) {
        super.beforeAddChild(child);
        Object cmp = ((ElementUI) child).getOuterComponent();
        
        if (cmp instanceof BaseUIComponent) {
            BaseUIComponent comp = (BaseUIComponent) cmp;
            comp.setWidth(null);
            comp.setHeight(null);
        }
    }
    
    public Alignment getAlignment() {
        return toolbar.getAlignment();
    }
    
    public void setAlignment(Alignment alignment) {
        toolbar.setAlignment(alignment);
    }
    
}
