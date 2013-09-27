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

import org.zkoss.zul.Caption;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

/**
 * Wraps the ZK Tab and Tabpanel components.
 */
public class UIElementTabPane extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementTabPane.class, UIElementTabView.class);
        registerAllowedChildClass(UIElementTabPane.class, UIElementBase.class);
    }
    
    private final Tab tab = new Tab();
    
    private final Caption caption = new Caption();
    
    private final Tabpanel tabPanel = new Tabpanel();
    
    /**
     * Set up the tab and tab panel ZK components. Note that we use a custom widget override to
     * allow setting the color of the caption text.
     */
    public UIElementTabPane() {
        super();
        setOuterComponent(tabPanel);
        associateComponent(tab);
        tabPanel.setSclass("cwf-tab-panel");
        tabPanel.setHeight("100%");
        tab.setSclass("cwf-tab");
        tab.setWidgetOverride(CUSTOM_COLOR_OVERRIDE,
            "function(value) {jq(this).find('.z-tab-text').css('color',value?value:'');}");
        tab.appendChild(caption);
        caption.setSclass("cwf-tab-caption");
    }
    
    /**
     * Make this tab pane active.
     */
    @Override
    public void bringToFront() {
        super.bringToFront();
        ((UIElementTabView) getParent()).setActivePane(this);
    }
    
    /**
     * Requires moving both ZK components.
     */
    @Override
    protected void afterMoveTo(int index) {
        moveChild(tab, index);
        moveChild(tabPanel, index);
    }
    
    /**
     * The caption label is the instance name.
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }
    
    /**
     * Sets the visibility of the tab and tab panel.
     */
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        tab.setSelected(activated);
        tab.setVisible(visible);
    }
    
    /**
     * Apply/remove the design context menu both tab and tab panel.
     * 
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    @Override
    protected void setDesignContextMenu(Menupopup contextMenu) {
        setDesignContextMenu(tabPanel, contextMenu);
        setDesignContextMenu(tab, contextMenu);
    }
    
    /**
     * Apply the disable style when a tab is disabled.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tab.setSclass(enabled ? "cwf-tab" : "cwf-tab-disabled");
    }
    
    /**
     * Applies color to the tab caption text as well as the tab panel.
     */
    @Override
    protected void applyColor() {
        super.applyColor();
        applyColor(tab);
        tabPanel.invalidate();
    }
    
    @Override
    protected void bind() {
        Tabbox tabbox = (Tabbox) getParent().getOuterComponent();
        tabbox.getTabs().appendChild(tab);
        tabbox.getTabpanels().appendChild(tabPanel);
    }
    
    @Override
    protected void unbind() {
        tab.detach();
        tabPanel.detach();
    }
    
    /*package*/Caption getCaption() {
        return caption;
    }
    
    /**
     * Returns the caption label.
     * 
     * @return
     */
    public String getLabel() {
        return caption.getLabel();
    }
    
    /**
     * Sets the caption label.
     * 
     * @param value
     */
    public void setLabel(String value) {
        caption.setLabel(value);
    }
    
    /**
     * Hint text should be applied to the tab.
     */
    @Override
    protected void applyHint() {
        tab.setTooltiptext(getHint());
    }
}
