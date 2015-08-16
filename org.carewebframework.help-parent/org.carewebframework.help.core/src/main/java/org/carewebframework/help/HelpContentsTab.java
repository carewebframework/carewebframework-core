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

import java.util.HashMap;
import java.util.Map;

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
    
    private final Map<HelpTopic, Treeitem> topics = new HashMap<HelpTopic, Treeitem>();
    
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
