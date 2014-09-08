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

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Span;

/**
 * A step-oriented UI Element. This is a composite element consisting of a button and its separator
 * with an associated pane. Clicking on a button activates its corresponding pane.
 */
public class UIElementStepPane extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementStepPane.class, UIElementStepView.class);
        registerAllowedChildClass(UIElementStepPane.class, UIElementBase.class);
    }
    
    private final Div pane = new Div();
    
    private final Button button = new Button("New Step");
    
    private final Span separator = new Span();
    
    private boolean isHomePane;
    
    /**
     * Create the ZK components that comprise this UI element.
     */
    public UIElementStepPane() {
        super();
        fullSize(pane);
        setOuterComponent(pane);
        associateComponent(button);
        associateComponent(separator);
        pane.setVisible(false);
        separator.setZclass("cwf-step-separator");
        button.setZclass("cwf-step-button");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                ((UIElementStepView) getParent()).setActivePane(UIElementStepPane.this);
                pane.setFocus(true);
            }
            
        });
    }
    
    /**
     * Called by parent step view to mark this pane as a home page.
     * 
     * @param value If true, this is the home page; false if not.
     */
    /*package*/void setHomePane(boolean value) {
        this.isHomePane = value;
        updateVisibility();
    }
    
    /**
     * Add the ZK components of the child pane to their respective parent components in the view.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#bind
     */
    @Override
    protected void bind() {
        super.bind();
        Component root = ((UIElementStepView) getParent()).getToolbarRoot();
        root.appendChild(button);
        root.appendChild(separator);
    }
    
    /**
     * Detach the ZK components of the child pane the UI.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#unbind
     */
    @Override
    protected void unbind() {
        super.unbind();
        button.detach();
        separator.detach();
    }
    
    /**
     * Brings this UI element to the front of the UI by making it the active pane.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#bringToFront()
     */
    @Override
    public void bringToFront() {
        super.bringToFront();
        ((UIElementStepView) getParent()).setActivePane(this);
    }
    
    /**
     * Changes the ordering of a button and its separator.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#afterMoveTo(int)
     */
    @Override
    protected void afterMoveTo(int index) {
        super.afterMoveTo(index);
        moveChild(button, index * 2);
        moveChild(separator, index * 2 + 1);
        updateParentState();
    }
    
    /**
     * Returns the instance name to use in the designer.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#getInstanceName()
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }
    
    /**
     * Apply color changes to button and pane only.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#applyColor()
     */
    @Override
    protected void applyColor() {
        ZKUtil.updateStyle(button, "color", getColor());
        ZKUtil.updateStyle(pane, "background", getColor());
    }
    
    /**
     * Overrides the default behavior to also target the button component.
     * 
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    @Override
    protected void setDesignContextMenu(Menupopup contextMenu) {
        super.setDesignContextMenu(contextMenu);
        setDesignContextMenu(button, contextMenu);
    }
    
    /**
     * Updates the button's styling based on the current state.
     */
    private void updateButtonStyle() {
        String zclass = button.getZclass();
        String sclass = isActivated() ? (zclass + "-selected ") : "";
        sclass += isEnabled() ? "" : (zclass + "-disabled");
        button.setSclass(sclass);
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);
        button.setVisible(visible && !isHomePane);
        separator.setVisible(button.isVisible() && getNextSibling(true) != null);
        updateButtonStyle();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateButtonStyle();
    }
    
    /**
     * Returns the button label.
     * 
     * @return The button's label.
     */
    public String getLabel() {
        return button.getLabel();
    }
    
    /**
     * Sets the button label.
     * 
     * @param value The label value.
     */
    public void setLabel(String value) {
        button.setLabel(value);
    }
    
    @Override
    protected void applyHint() {
        button.setTooltiptext(getHint());
    }
    
    /**
     * Returns the button image url.
     * 
     * @return Url for the button image. May be null or empty.
     */
    public String getIcon() {
        return button.getImage();
    }
    
    /**
     * Sets the button image from a url.
     * 
     * @param value Url for the button image. May be null or empty.
     */
    public void setIcon(String value) {
        button.setImage(value);
        button.invalidate();
    }
}
