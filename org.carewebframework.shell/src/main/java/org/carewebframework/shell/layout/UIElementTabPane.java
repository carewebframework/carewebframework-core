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
    
    /**
     * Re-purpose close tab button for drop down menu.
     */
    public static class TabEx extends Tab {
        
        private static final long serialVersionUID = 1L;
        
        public TabEx() {
            super();
            setSclass("cwf-tab");
            ZKUtil.setCustomColorLogic(this, "jq(this).find('.z-tab-text').css('color',value?value:'');");
        }
        
        @Override
        public void onClose() {
            // Ignore
        }
    };
    
    private final INotificationListener badgeListener = new UIElementBase.INotificationListener() {
        
        @Override
        public void onNotification(UIElementBase sender, String eventName, Object eventData) {
            ZKUtil.setBadge("#" + tab.getUuid() + "-cnt", (String) eventData);
        }
        
    };
    
    private final TabEx tab = new TabEx();
    
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
        listenToChild("badge", badgeListener);
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
     * Sets the visibility and selection state of the tab.
     */
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        tab.setVisible(visible);
        tab.setSelected(visible && activated);
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
        ZKUtil.toggleSclass(tab, "cwf-tab", "cwf-tab-disabled", enabled);
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
    
    /*package*/Tab getTab() {
        return tab;
    }
    
    /**
     * Returns the caption label.
     * 
     * @return The caption label.
     */
    public String getLabel() {
        return tab.getLabel();
    }
    
    /**
     * Sets the caption label.
     * 
     * @param value The caption label.
     */
    public void setLabel(String value) {
        tab.setLabel(value);
    }
    
    /**
     * Returns the tab icon.
     * 
     * @return The tab icon.
     */
    public String getIcon() {
        return tab.getImage();
    }
    
    /**
     * Sets the tab icon.
     * 
     * @param value The tab icon.
     */
    public void setIcon(String value) {
        tab.setImage(value);
    }
    
    /**
     * Hint text should be applied to the tab.
     */
    @Override
    protected void applyHint() {
        tab.setTooltiptext(getHint());
    }
}
