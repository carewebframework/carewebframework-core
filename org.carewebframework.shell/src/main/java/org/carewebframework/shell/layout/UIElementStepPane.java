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

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Hyperlink;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Span;

/**
 * A step-oriented UI Element. This is a composite element consisting of a button and its separator
 * with an associated pane. Clicking on a button activates its corresponding pane.
 */
public class UIElementStepPane extends UIElementCWFBase {
    
    static {
        registerAllowedParentClass(UIElementStepPane.class, UIElementStepView.class);
        registerAllowedChildClass(UIElementStepPane.class, UIElementBase.class);
    }
    
    private final Div pane = new Div();
    
    private final Hyperlink button = new Hyperlink();
    
    private final Span step = new Span();
    
    private boolean isHomePane;
    
    /**
     * Create the UI components that comprise this UI element.
     */
    public UIElementStepPane() {
        super();
        button.setLabel("New Step");
        fullSize(pane);
        setOuterComponent(pane);
        associateComponent(button);
        associateComponent(step);
        pane.setVisible(false);
        step.addChild(button);
        button.addClass("cwf-step-button");
        button.addClass("btn btn-sm");
        button.addEventListener("click", (event) -> {
            ((UIElementStepView) getParent()).setActivePane(UIElementStepPane.this);
            pane.setFocus(true);
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
     * Add the UI components of the child pane to their respective parent components in the view.
     * 
     * @see org.carewebframework.shell.layout.UIElementCWFBase#bind
     */
    @Override
    protected void bind() {
        super.bind();
        BaseComponent root = ((UIElementStepView) getParent()).getToolbarRoot();
        root.addChild(step);
    }
    
    /**
     * Detach the UI components of the child pane the UI.
     * 
     * @see org.carewebframework.shell.layout.UIElementCWFBase#unbind
     */
    @Override
    protected void unbind() {
        super.unbind();
        step.detach();
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
     * @see org.carewebframework.shell.layout.UIElementCWFBase#afterMoveTo(int)
     */
    @Override
    protected void afterMoveTo(int index) {
        super.afterMoveTo(index);
        moveChild(step, index);
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
     * @see org.carewebframework.shell.layout.UIElementCWFBase#applyColor()
     */
    @Override
    protected void applyColor() {
        button.addStyle("color", getColor());
        pane.addStyle("background", getColor());
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
        button.addStyle("disabled", isEnabled() ? "true" : null);
        button.addClass(isActivated() ? "flavor:btn-primary" : "flavor:btn-default");
    }
    
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);
        step.setVisible(visible && !isHomePane);
        step.addClass(getNextSibling(true) != null ? "cwf-step-separator" : null);
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
        button.setHint(getHint());
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
    }
}
