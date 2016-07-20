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
import org.carewebframework.ui.zk.TreeUtil;

import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

/**
 * Tab for displaying the table of contents. This is displayed as a tree of topics.
 */
public class HelpContentsTab extends HelpTab {
    
    private static final long serialVersionUID = 1L;
    
    private Tree tree;
    
    private HelpTopic selectedTopic;
    
    private final Map<HelpTopic, Treeitem> topics = new HashMap<>();
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer The help viewer.
     * @param viewType The view type.
     */
    public HelpContentsTab(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpContentsTab.zul");
    }
    
    /**
     * Change the viewer's active topic when a tree item is selected.
     */
    public void onSelect$tree() {
        Treeitem item = tree.getSelectedItem();
        selectedTopic = item == null ? null : (HelpTopic) item.getValue();
        setTopic(selectedTopic);
    }
    
    /**
     * Sort the table of contents before initial display.
     * 
     * @see HelpTab#init()
     */
    @Override
    public void init() {
        super.init();
        TreeUtil.sort(tree.getTreechildren(), false);
    }
    
    /**
     * When the viewer changes the topic selection, highlight that topic in the tree (if it exists).
     * 
     * @see HelpTab#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        if (topic != selectedTopic) {
            selectedTopic = topic;
            tree.setSelectedItem(topics.get(topic));
        }
    }
    
    /**
     * Merges the table of contents entries from the specified view into the tree.
     * 
     * @see HelpTab#addView(IHelpView)
     */
    @Override
    public void addView(IHelpView view) {
        super.addView(view);
        HelpTopicNode topNode = view.getTopicTree();
        
        for (HelpTopicNode node : topNode.getChildren()) {
            addNode(tree.getTreechildren(), node).getTreerow().setSclass("cwf-help-toc-top");
        }
    }
    
    /**
     * Recursively create tree items that correspond to the topic tree from the help view.
     * 
     * @param tc The treechildren component to receive newly created tree items.
     * @param node A topic tree node.
     * @return Newly created tree item.
     */
    private Treeitem addNode(Treechildren tc, HelpTopicNode node) {
        HelpTopic topic = node.getTopic();
        Treeitem parent = new Treeitem(topic.getLabel(), topic);
        topics.put(topic, parent);
        tc.appendChild(parent);
        tc = null;
        
        for (HelpTopicNode child : node.getChildren()) {
            if (tc == null) {
                tc = new Treechildren();
                parent.appendChild(tc);
            }
            
            addNode(tc, child);
        }
        
        parent.setOpen(true);
        return parent;
    }
    
}
