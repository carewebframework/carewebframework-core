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
package org.carewebframework.help.viewer;

import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;
import org.carewebframework.help.viewer.HelpHistory.ITopicListener;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Tab;
import org.carewebframework.web.component.Tabview;
import org.carewebframework.web.page.PageUtil;

/**
 * Abstract base class for all help tabs. It descends from Tab.
 */
public abstract class HelpTab extends Tab implements INamespace, ITopicListener {
    
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
        Class<? extends HelpTab> tabClass = viewType == null ? null : getTabClass(viewType);
        
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
     * Returns the help tab class that services the specified view type. For unsupported view types,
     * returns null.
     * 
     * @param viewType The view type.
     * @return A help tab class.
     */
    private static Class<? extends HelpTab> getTabClass(HelpViewType viewType) {
        switch (viewType) {
            case TOC:
                return HelpContentsTab.class;
            
            case KEYWORD:
                return HelpIndexTab.class;
            
            case INDEX:
                return HelpIndexTab.class;
            
            case SEARCH:
                return HelpSearchTab.class;
            
            case HISTORY:
                return HelpHistoryTab.class;
            
            case GLOSSARY:
                return HelpIndexTab.class;
            
            default:
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
        addStyle("overflow", "auto");
        addToTabbox();
        String label = StrUtil.getLabel("cwf.help.tab." + viewType.name().toLowerCase() + ".label");
        setLabel(label == null ? viewType.name() : label);
        
        if (zulTemplate != null) {
            PageUtil.createPage(HelpUtil.RESOURCE_PREFIX + zulTemplate, this);
            wireController(this);
        }
    }
    
    /**
     * Adds this tab to the parent tab box. The view type ordinal value determines the sequencing of
     * tabs.
     */
    private void addToTabbox() {
        int pos = -1;
        Tabview parent = viewer.getTabview();
        List<BaseComponent> children = parent.getChildren();
        
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            
            if (child instanceof HelpTab) {
                if (viewType.compareTo(((HelpTab) child).viewType) <= 0) {
                    pos = i;
                    break;
                }
            }
        }
        
        insertChild(parent, this, pos);
    }
    
    /**
     * Adds the child component to the parent at the specified position.
     * 
     * @param parent Parent component to receive the child.
     * @param child Child component to be added.
     * @param pos Position for the child component relative to its siblings. If negative, the child
     *            is added after its siblings.
     */
    private void insertChild(BaseComponent parent, BaseComponent child, int pos) {
        parent.addChild(child, pos);
    }
    
    /**
     * Adds a view to the tab. Subclasses should override this to merge resources from the view.
     * When doing so, always call the super method.
     * 
     * @param view The view to add.
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
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        
    }
    
}
