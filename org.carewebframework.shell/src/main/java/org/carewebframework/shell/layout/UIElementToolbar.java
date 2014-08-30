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

import org.carewebframework.shell.designer.PropertyEditorOrderedChildren;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Toolbar;

/**
 * Implements a shared toolbar.
 */
public class UIElementToolbar extends UIElementZKBase {
    
    static {
        registerAllowedChildClass(UIElementToolbar.class, UIElementBase.class);
        registerAllowedParentClass(UIElementToolbar.class, UIElementBase.class);
        PropertyTypeRegistry.register("children", null, PropertyEditorOrderedChildren.class);
    }
    
    private final Toolbar toolbar;
    
    public UIElementToolbar() {
        this(new Toolbar());
    }
    
    public UIElementToolbar(Toolbar toolbar) {
        super();
        this.toolbar = toolbar;
        toolbar.setStyle("align:right");
        setOuterComponent(toolbar);
        maxChildren = Integer.MAX_VALUE;
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
    public void addToolbarComponent(Component component, String action) {
        Component ref = toolbar.getFirstChild();
        
        if (component instanceof Toolbar) {
            Component child;
            
            while ((child = component.getFirstChild()) != null) {
                toolbar.insertBefore(child, ref);
            }
            
        } else {
            toolbar.insertBefore(component, ref);
            ActionListener.addAction(component, action);
        }
    }
    
    @Override
    protected void beforeAddChild(UIElementBase child) {
        super.beforeAddChild(child);
        Object cmp = child.getOuterComponent();
        
        if (cmp instanceof HtmlBasedComponent) {
            HtmlBasedComponent comp = (HtmlBasedComponent) cmp;
            comp.setWidth(null);
            comp.setHeight(null);
        }
    }
    
    @Override
    protected void beforeRemoveChild(UIElementBase child) {
        super.beforeRemoveChild(child);
    }
    
    /**
     * Dummy component made visible during design mode to ensure that an otherwise empty toolbar is
     * always visible.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        ZKUtil.updateStyle(toolbar, "min-height", designMode ? "20px" : null);
    }
    
}
