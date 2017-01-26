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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpTopicNode;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;
import org.carewebframework.ui.util.TreeUtil;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;

/**
 * Tab for displaying the table of contents. This is displayed as a tree of topics.
 */
public class HelpViewContents extends HelpViewBase {
    
    @WiredComponent
    private Treeview tree;
    
    private HelpTopic selectedTopic;
    
    private final Map<HelpTopic, Treenode> topics = new HashMap<>();
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer The help viewer.
     * @param viewType The view type.
     */
    public HelpViewContents(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpContentsTab.cwf");
    }
    
    /**
     * Change the viewer's active topic when a tree item is selected.
     */
    @EventHandler(value = "change", target = "@tree")
    private void onChange$tree() {
        Treenode item = tree.getSelectedNode();
        selectedTopic = item == null ? null : (HelpTopic) item.getData();
        setTopic(selectedTopic);
    }
    
    /**
     * Sort the table of contents before initial display.
     * 
     * @see HelpViewBase#init()
     */
    @Override
    public void init() {
        super.init();
        TreeUtil.sort(tree, false);
    }
    
    /**
     * When the viewer changes the topic selection, highlight that topic in the tree (if it exists).
     * 
     * @see HelpViewBase#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        if (topic != selectedTopic) {
            selectedTopic = topic;
            tree.setSelectedNode(topics.get(topic));
        }
    }
    
    /**
     * Merges the table of contents entries from the specified view into the tree.
     * 
     * @see HelpViewBase#addView(IHelpView)
     */
    @Override
    public void addView(IHelpView view) {
        super.addView(view);
        HelpTopicNode topNode = view.getTopicTree();
        
        for (HelpTopicNode node : topNode.getChildren()) {
            addNode(tree, node); //TODO: .getTreerow().setSclass("cwf-help-toc-top");
        }
    }
    
    /**
     * Recursively create tree items that correspond to the topic tree from the help view.
     * 
     * @param tc The root node to receive newly created tree nodes.
     * @param node A topic tree node.
     * @return Newly created tree node.
     */
    private Treenode addNode(BaseComponent tc, HelpTopicNode node) {
        HelpTopic topic = node.getTopic();
        Treenode parent = new Treenode();
        parent.setLabel(topic.getLabel());
        parent.setData(topic);
        topics.put(topic, parent);
        tc.addChild(parent);
        
        for (HelpTopicNode child : node.getChildren()) {
            addNode(tc, child);
        }
        
        parent.setCollapsed(false);
        return parent;
    }
    
}
