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

import org.carewebframework.shell.designer.PropertyEditorStepView;
import org.carewebframework.shell.layout.LayoutConstants;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.theme.ThemeUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Window;

/**
 * A step-oriented UI Element. This is implemented as a window component with a top toolbar
 * containing sequential steps as a sequence of buttons.
 */
public class UIElementStepView extends UIElementBase {
    
    static {
        registerAllowedParentClass(UIElementStepView.class, UIElementBase.class);
        registerAllowedChildClass(UIElementStepView.class, UIElementStepPane.class);
        PropertyTypeRegistry.register("step", PropertyEditorStepView.class);
    }
    
    private final Window outer;
    
    private BaseUIComponent inner;
    
    private Label lblTitle;
    
    private BaseUIComponent tbarCenter;
    
    private Button btnLeft;
    
    private Button btnRight;
    
    private Button btnHome;
    
    private UIElementStepPane activePane;
    
    private boolean noNavigation;
    
    private boolean noHome;
    
    private final String defaultHomeIcon;
    
    private ThemeUtil.PanelStyle style = ThemeUtil.PanelStyle.PRIMARY;
    
    /**
     * Creates the UI components that comprise this UI element.
     */
    public UIElementStepView() {
        super();
        maxChildren = Integer.MAX_VALUE;
        outer = (Window) createFromTemplate();
        setOuterComponent(outer);
        setInnerComponent(inner);
        associateComponent(tbarCenter);
        defaultHomeIcon = btnHome.getImage();
    }
    
    @Override
    protected void beforeRemoveChild(UIElementBase child) {
        if (child == activePane) {
            setActivePane(null);
        }
    }
    
    /**
     * Activates the active pane. If no pane is currently active, activate the first pane.
     * 
     * @see org.carewebframework.shell.elements.UIElementBase#activateChildren(boolean)
     */
    @Override
    public void activateChildren(boolean activate) {
        setActivePane(activePane == null ? (UIElementStepPane) getFirstVisibleChild() : activePane);
    }
    
    /**
     * Url for zul template.
     */
    @Override
    protected String getTemplateUrl() {
        return LayoutConstants.RESOURCE_PREFIX + "UIElementStepView.cwf";
    }
    
    /**
     * Sets the caption.
     * 
     * @param caption The caption.
     */
    public void setCaption(String caption) {
        lblTitle.setLabel(caption);
    }
    
    /**
     * Returns the caption of the panel.
     * 
     * @return The panel caption.
     */
    public String getCaption() {
        return lblTitle.getLabel();
    }
    
    /**
     * Returns the currently active pane, or null if no pane is active.
     * 
     * @return The currently active pane.
     */
    public UIElementStepPane getActivePane() {
        return activePane;
    }
    
    /**
     * Sets the active pane. Since only one pane can be active at a time, the currently active pane
     * is first deactivated.
     * 
     * @param pane Step pane to activate.
     */
    public void setActivePane(UIElementStepPane pane) {
        if (pane == activePane) {
            return;
        }
        
        if (activePane != null) {
            activePane.activate(false);
        }
        
        activePane = hasChild(pane) ? pane : null;
        
        if (activePane != null) {
            activePane.activate(true);
        }
        
        btnHome.toggleClass("z-toolbarbutton-checked", null, activePane == null || activePane.getIndex() != 0);
        updateNavigationElements();
    }
    
    /**
     * Returns the index of the active pane, or -1 if no pane is active.
     * 
     * @return The index of the active pane.
     */
    public int getActivePaneIndex() {
        return activePane == null ? -1 : indexOfChild(activePane);
    }
    
    /*package*/BaseComponent getToolbarRoot() {
        return tbarCenter;
    }
    
    /**
     * Updates the state of child components. This logic ensures that the separator following the
     * last button in the step sequence is hidden.
     * 
     * @see org.carewebframework.shell.elements.UIElementBase#updateState()
     */
    @Override
    protected void updateState() {
        String homeIcon = null;
        
        for (int i = 0; i < getChildCount(); i++) {
            UIElementStepPane pane = (UIElementStepPane) getChild(i);
            pane.setHomePane(!noHome && i == 0);
            
            if (i == 0) {
                homeIcon = pane.getIcon();
            }
        }
        
        btnHome.setImage(homeIcon == null ? defaultHomeIcon : homeIcon);
        updateNavigationElements();
        super.updateState();
    }
    
    /**
     * Update any navigation elements based on the currently active pane.
     */
    protected void updateNavigationElements() {
        int i = getActivePaneIndex();
        boolean anyVisible = nextVisiblePaneIndex(true, noHome ? -1 : 0) >= 0;
        btnHome.setVisible(!noHome && getChildCount() > 0);
        btnLeft.setVisible(!noNavigation && anyVisible);
        btnRight.setVisible(!noNavigation && anyVisible);
        btnLeft.setDisabled(nextVisiblePaneIndex(false, i) < (noHome ? 0 : 1));
        btnRight.setDisabled(nextVisiblePaneIndex(true, i) == -1);
    }
    
    /**
     * Returns the index of the next visible pane before/after the specified one.
     * 
     * @param forward If true, search forward. Otherwise, search backward.
     * @param fromIndex Pane index from which to search.
     * @return Index of the next visible pane.
     */
    protected int nextVisiblePaneIndex(boolean forward, int fromIndex) {
        int max = getChildCount();
        int idx = fromIndex >= 0 ? fromIndex : forward ? -1 : max;
        idx += forward ? 1 : -1;
        
        while (idx > -1 && idx < max) {
            UIElementStepPane pane = (UIElementStepPane) getChild(idx);
            
            if (pane.isVisible()) {
                return idx;
            }
            
            idx += forward ? 1 : -1;
        }
        
        return -1;
    }
    
    /**
     * Returns the next visible pane before/after the specified one.
     * 
     * @param forward If true, search forward. Otherwise, search backward.
     * @param fromIndex Pane index from which to search.
     * @return The next visible pane.
     */
    protected UIElementStepPane nextVisiblePane(boolean forward, int fromIndex) {
        int idx = nextVisiblePaneIndex(forward, fromIndex);
        return idx == -1 ? null : (UIElementStepPane) getChild(idx);
    }
    
    @Override
    protected void afterMoveChild(UIElementBase child, UIElementBase before) {
        super.afterMoveChild(child, before);
        UIElementStepPane childpane = (UIElementStepPane) child;
        UIElementStepPane beforepane = (UIElementStepPane) before;
        moveChild(childpane.getStep(), beforepane.getStep());
    }
    
    /**
     * Navigate one pane forward or backward.
     * 
     * @param forward Direction of navigation (false moves left, true moves right).
     */
    private void navigate(boolean forward) {
        setActivePane(nextVisiblePane(forward, getActivePaneIndex()));
    }
    
    /**
     * Navigate one pane left.
     */
    public void onClick$btnLeft() {
        navigate(false);
    }
    
    /**
     * Navigate one pane right.
     */
    public void onClick$btnRight() {
        navigate(true);
    }
    
    /**
     * Activate the home pane.
     */
    public void onClick$btnHome() {
        setActivePane((UIElementStepPane) getFirstChild());
    }
    
    /**
     * Returns true if navigation controls are suppressed.
     * 
     * @return True if navigation controls are suppressed.
     */
    public boolean getNoNavigation() {
        return noNavigation;
    }
    
    /**
     * Set to true to suppress navigation controls.
     * 
     * @param noNavigation True to suppress navigation controls.
     */
    public void setNoNavigation(boolean noNavigation) {
        this.noNavigation = noNavigation;
        updateSclass();
        updateNavigationElements();
    }
    
    /**
     * Returns true if home pane is suppressed.
     * 
     * @return Home suppression flag.
     */
    public boolean getNoHome() {
        return noHome;
    }
    
    /**
     * Set to true to suppress home pane.
     * 
     * @param noHome Home suppression flag.
     */
    public void setNoHome(boolean noHome) {
        this.noHome = noHome;
        updateSclass();
        updateState();
    }
    
    /**
     * Returns the panel style to use for the desktop.
     * 
     * @return The panel style.
     */
    public ThemeUtil.PanelStyle getStyle() {
        return style;
    }
    
    /**
     * Sets the panel style to use for the desktop.
     * 
     * @param style The panel style.
     */
    public void setStyle(ThemeUtil.PanelStyle style) {
        this.style = style;
        outer.addClass(style.getThemeClass());
    }
    
    private void updateSclass() {
        //TODO: ZKUtil.updateSclass(outer.getTitle(), "cwf-step-nonav", !noNavigation);
        //TODO: ZKUtil.updateSclass(outer.getTitle(), "cwf-step-nohome", !noHome);
    }
}
