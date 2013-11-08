/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import java.util.List;

import org.carewebframework.help.HelpHistory.ITopicListener;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

/**
 * Abstract base class for all help tabs. It descends from Tabpanel and also creates and manages the
 * associated Tab component.
 */
public abstract class HelpTab extends Tabpanel implements IdSpace, ITopicListener {
    
    private static final long serialVersionUID = 1L;
    
    private final Tab tab = new Tab();
    
    private final HelpViewType viewType;
    
    private final HelpViewer viewer;
    
    private boolean initialized;
    
    /**
     * Creates a new tab for the specified view type.
     * 
     * @param viewer The help viewer instance.
     * @param viewType The view type supported by the created tab.
     * @return The help tab that supports the specified view type.
     */
    public static HelpTab createTab(HelpViewer viewer, HelpViewType viewType) {
        Class<? extends HelpTab> tabClass = viewType == null ? null : viewType.getTabClass();
        
        if (tabClass == null) {
            return null;
        }
        
        try {
            return tabClass.getConstructor(HelpViewer.class, HelpViewType.class).newInstance(viewer, viewType);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Main constructor.
     * 
     * @param viewer The help viewer.
     * @param viewType The view type supported by the created tab.
     * @param zulTemplate The zul page that specifies the layout for the tab.
     */
    public HelpTab(HelpViewer viewer, HelpViewType viewType, String zulTemplate) {
        super();
        this.viewer = viewer;
        this.viewType = viewType;
        setStyle("overflow:auto");
        addToTabbox();
        String label = Labels.getLabel("cwf.help.tab." + viewType.name().toLowerCase() + ".label");
        tab.setLabel(label == null ? viewType.name() : label);
        tab.addForward(Events.ON_SELECT, this, null);
        
        if (zulTemplate != null) {
            Executions.createComponents(HelpUtil.RESOURCE_PREFIX + zulTemplate, this, null);
            ZKUtil.wireController(this);
        }
    }
    
    /**
     * Adds this tab to the parent tab box. The view type ordinal value determines the sequencing of
     * tabs.
     */
    private void addToTabbox() {
        int pos = -1;
        Tabbox parent = viewer.getTabbox();
        List<?> children = parent.getTabpanels().getChildren();
        
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            
            if (child instanceof HelpTab) {
                if (viewType.compareTo(((HelpTab) child).viewType) <= 0) {
                    pos = i;
                    break;
                }
            }
        }
        
        insertChild(parent.getTabpanels(), this, pos);
        insertChild(parent.getTabs(), tab, pos);
    }
    
    /**
     * Adds the child component to the parent at the specified position.
     * 
     * @param parent Parent component to receive the child.
     * @param child Child component to be added.
     * @param pos Position for the child component relative to its siblings. If negative, the child
     *            is added after its siblings.
     */
    private void insertChild(Component parent, Component child, int pos) {
        if (pos < 0) {
            parent.appendChild(child);
        } else {
            parent.insertBefore(child, parent.getChildren().get(pos));
        }
    }
    
    /**
     * Adds a view to the tab. Subclasses should override this to merge resources from the view.
     * When doing so, always call the super method.
     * 
     * @param view
     */
    public void addView(IHelpView view) {
        initialized = false;
    }
    
    /**
     * Convenience method for setting the currently selected topic in the viewer.
     * 
     * @param topic HelpTopic to select in the viewer.
     */
    protected void setTopic(HelpTopic topic) {
        viewer.setTopic(topic);
    }
    
    /**
     * Called when the tab is selected.
     */
    public void onSelect() {
        if (!initialized) {
            initialized = true;
            init();
        }
    }
    
    /**
     * Called prior to initial display of the tab to provide any final initialization (e.g., to sort
     * a list or tree).
     */
    protected void init() {
    }
    
    /**
     * Returns the view type associated with the tab.
     * 
     * @return The associated view type.
     */
    public HelpViewType getViewType() {
        return viewType;
    }
    
    /**
     * Called when a new topic is selected in the viewer. Override to provide any special actions
     * (like selecting the corresponding UI element). Note that the originator of the topic change
     * may be the same tab.
     * 
     * @see org.carewebframework.help.HelpHistory.ITopicListener#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        
    }
    
    /**
     * Overridden to propagate visibility changes to the associated tab.
     * 
     * @see org.zkoss.zk.ui.AbstractComponent#setVisible(boolean)
     */
    @Override
    public boolean setVisible(boolean visible) {
        tab.setVisible(visible);
        return super.setVisible(visible);
    }
}
