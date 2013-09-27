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

import org.carewebframework.shell.designer.PropertyEditorStepView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Panel;

/**
 * A step-oriented UI Element. This is implemented as a ZK panel component with a top toolbar
 * containing sequential steps as a sequence of buttons.
 */
public class UIElementStepView extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementStepView.class, UIElementBase.class);
        registerAllowedChildClass(UIElementStepView.class, UIElementStepPane.class);
        PropertyTypeRegistry.register("step", null, PropertyEditorStepView.class);
    }
    
    private final Component outer;
    
    private Component inner;
    
    private HtmlBasedComponent toolbar;
    
    private HtmlBasedComponent tbarCenter;
    
    private Button btnLeft;
    
    private Button btnRight;
    
    private Button btnHome;
    
    private Panel panel;
    
    private UIElementStepPane activePane;
    
    private boolean noNavigation;
    
    private boolean noHome;
    
    private final String defaultHomeIcon;
    
    /**
     * Creates the ZK components that comprise this UI element.
     * 
     * @throws Exception
     */
    public UIElementStepView() throws Exception {
        super();
        maxChildren = Integer.MAX_VALUE;
        outer = createFromTemplate();
        setOuterComponent(outer);
        setInnerComponent(inner);
        associateComponent(tbarCenter);
        setColor("#1F4D69");
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
     * @see org.carewebframework.shell.layout.UIElementBase#activateChildren(boolean)
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
        return LayoutConstants.RESOURCE_PREFIX + "UIElementStepView.zul";
    }
    
    /**
     * Sets the caption of the panel.
     * 
     * @param caption
     */
    public void setCaption(String caption) {
        panel.setTitle(caption);
    }
    
    /**
     * Returns the caption of the panel.
     * 
     * @return The panel caption.
     */
    public String getCaption() {
        return panel.getTitle();
    }
    
    /**
     * Apply color changes to toolbar only.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#applyColor()
     */
    @Override
    public void applyColor() {
        ZKUtil.updateStyle(toolbar, "background-color", getColor());
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
        if (activePane != null) {
            activePane.activate(false);
        }
        
        activePane = hasChild(pane) ? pane : null;
        
        if (activePane != null) {
            activePane.activate(true);
        }
        
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
    
    /*package*/Component getToolbarRoot() {
        return tbarCenter;
    }
    
    /**
     * Updates the state of child components. This logic ensures that the separator following the
     * last button in the step sequence is hidden.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#updateState()
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
        Clients.resize(panel);
    }
    
    /**
     * Returns the index of the next visible pane before/after the specified one.
     * 
     * @param forward If true, search forward. Otherwise, search backward.
     * @param fromIndex Pane index from which to search.
     * @return
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
     * @return
     */
    protected UIElementStepPane nextVisiblePane(boolean forward, int fromIndex) {
        int idx = nextVisiblePaneIndex(forward, fromIndex);
        return idx == -1 ? null : (UIElementStepPane) getChild(idx);
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
     * @return
     */
    public boolean getNoNavigation() {
        return noNavigation;
    }
    
    /**
     * Set to true to suppress navigation controls.
     * 
     * @param noNavigation
     */
    public void setNoNavigation(boolean noNavigation) {
        this.noNavigation = noNavigation;
        updateSclass();
        updateNavigationElements();
    }
    
    /**
     * Returns true if home pane is suppressed.
     * 
     * @return
     */
    public boolean getNoHome() {
        return noHome;
    }
    
    /**
     * Set to true to suppress home pane.
     * 
     * @param noHome
     */
    public void setNoHome(boolean noHome) {
        this.noHome = noHome;
        updateSclass();
        updateState();
    }
    
    private void updateSclass() {
        toolbar.setSclass("cwf-step-toolbar" + (noNavigation ? " cwf-step-nonav" : "") + (noHome ? " cwf-step-nohome" : ""));
    }
}
