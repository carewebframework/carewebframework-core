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

import org.carewebframework.shell.designer.PropertyEditorTreeView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.zk.TreeUtil;

import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.LayoutRegion;
import org.zkoss.zul.Tree;

/**
 * A tree view consists of a selector pane on the left containing a ZK tree component and a view
 * area on the right where tree panes are positioned. Clicking on a node in the tree activates the
 * associated tree pane.
 */
public class UIElementTreeView extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementTreeView.class, UIElementBase.class);
        registerAllowedChildClass(UIElementTreeView.class, UIElementTreePane.class);
        PropertyTypeRegistry.register("nodes", null, PropertyEditorTreeView.class);
    }
    
    private final EventListener<SelectEvent<?, ?>> selectListener = new EventListener<SelectEvent<?, ?>>() {
        
        @Override
        public void onEvent(SelectEvent<?, ?> event) throws Exception {
            UIElementTreePane pane = (UIElementTreePane) getAssociatedUIElement(event.getReference());
            
            if (pane != null) {
                setActivePane(pane);
            }
        }
    };
    
    private Div innerPane;
    
    private Tree tree;
    
    private LayoutRegion selectorPane;
    
    private UIElementTreePane activePane;
    
    public UIElementTreeView() throws Exception {
        super();
        maxChildren = Integer.MAX_VALUE;
        setOuterComponent(createFromTemplate());
        setInnerComponent(innerPane);
        tree.addEventListener(Events.ON_SELECT, selectListener);
    }
    
    /**
     * Sets the caption of the selector pane.
     * 
     * @param value Selector pane caption.
     */
    public void setCaption(String value) {
        selectorPane.setTitle(value);
    }
    
    /**
     * Returns the caption of the selector pane.
     * 
     * @return Selector pane caption.
     */
    public String getCaption() {
        return selectorPane.getTitle();
    }
    
    /**
     * Opens or closes the selector pane.
     * 
     * @param value True to open the selector pane; false to close it.
     */
    public void setOpen(boolean value) {
        selectorPane.setOpen(value);
    }
    
    /**
     * Returns true if the selector pane is open.
     * 
     * @return True if selector pane is open.
     */
    public boolean isOpen() {
        return selectorPane.isOpen();
    }
    
    /**
     * Remove the associated tree node when a tree pane is removed.
     */
    @Override
    protected void beforeRemoveChild(UIElementBase child) {
        if (child == activePane) {
            setActivePane(null);
        }
        super.beforeRemoveChild(child);
    }
    
    /*package*/Tree getTree() {
        return tree;
    }
    
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        TreeUtil.adjustVisibility(tree);
    }
    
    /**
     * Only the active pane should receive the activation request.
     */
    @Override
    public void activateChildren(boolean activate) {
        if (activePane == null) {
            activePane = (UIElementTreePane) getAssociatedUIElement(tree.getSelectedItem());
        }
        
        if (activePane != null) {
            activePane.activate(activate);
        }
    }
    
    /**
     * Activates the specified pane. Any previously active pane will be deactivated.
     * 
     * @param pane Pane to make active.
     */
    protected void setActivePane(UIElementTreePane pane) {
        if (activePane != null) {
            activePane.makeActivePane(false);
        }
        
        activePane = pane;
        
        if (activePane != null) {
            activePane.makeActivePane(true);
        }
    }
}
