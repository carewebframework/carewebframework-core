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

import org.carewebframework.shell.designer.PropertyEditorTabView;
import org.carewebframework.shell.property.PropertyTypeRegistry;

import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Tabbox;

/**
 * Wraps the ZK Tabbox component. This UI element can only accept UIElementTabPane elements as
 * children and only one of those can be active at a time.
 */
public class UIElementTabView extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementTabView.class, UIElementBase.class);
        registerAllowedChildClass(UIElementTabView.class, UIElementTabPane.class);
        PropertyTypeRegistry.register("tabs", null, PropertyEditorTabView.class);
    }
    
    private final Tabbox tabBox;
    
    private UIElementTabPane activePane;
    
    public UIElementTabView() throws Exception {
        super();
        maxChildren = Integer.MAX_VALUE;
        tabBox = (Tabbox) createFromTemplate();
        setOuterComponent(tabBox);
        tabBox.setSclass("cwf-tabbox");
        tabBox.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<?, ?>>() {
            
            @Override
            public void onEvent(SelectEvent<?, ?> event) throws Exception {
                setActivePane((UIElementTabPane) getAssociatedUIElement(event.getTarget()));
            }
            
        });
    }
    
    /**
     * Sets the orientation which can be horizontal or vertical.
     * 
     * @param orientation Orientation setting.
     */
    public void setOrientation(String orientation) {
        if ("accordion".equals(orientation)) {
            tabBox.setOrient("horizontal");
            tabBox.setMold("accordion");
        } else {
            tabBox.setMold(null);
            tabBox.setOrient(orientation);
        }
    }
    
    /**
     * Returns the orientation (horizontal, vertical or accordion).
     * 
     * @return Orientation setting.
     */
    public String getOrientation() {
        return "accordion".equals(tabBox.getMold()) ? "accordion" : tabBox.getOrient();
    }
    
    /**
     * Need to detach both the tab and the tab panel of the child component.
     */
    @Override
    protected void beforeRemoveChild(UIElementBase child) {
        if (child == activePane) {
            setActivePane(null);
        }
    }
    
    /**
     * Sets the active (visible) pane.
     * 
     * @param pane The pane to become active.
     */
    protected void setActivePane(UIElementTabPane pane) {
        if (activePane != null) {
            activePane.activate(false);
        }
        
        activePane = pane;
        
        if (activePane != null) {
            activePane.activate(true);
        }
    }
    
    /**
     * Overrides activateChildren to ensure that only the active pane is affected.
     */
    @Override
    public void activateChildren(boolean activate) {
        if (activePane == null) {
            activePane = (UIElementTabPane) getAssociatedUIElement(tabBox.getSelectedTab());
        }
        
        if (activePane != null) {
            activePane.activate(activate);
        }
    }
    
}
