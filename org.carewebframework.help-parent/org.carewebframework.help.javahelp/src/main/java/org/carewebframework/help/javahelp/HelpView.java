/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.javahelp;

import java.net.MalformedURLException;

import javax.help.Map.ID;
import javax.help.NavigatorView;
import javax.help.TreeItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.beanutils.MethodUtils;

import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpTopicNode;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;

/**
 * IHelpView implementation for JavaHelp navigator views.
 */
public class HelpView implements IHelpView {
    
    protected final NavigatorView view;
    
    private final HelpViewType viewType;
    
    private final HelpTopicNode rootNode = new HelpTopicNode(null);
    
    /**
     * Create help view for given navigator view and view type.
     * 
     * @param view JavaHelp navigator view.
     * @param viewType View type.
     */
    public HelpView(NavigatorView view, HelpViewType viewType) {
        this.view = view;
        this.viewType = viewType;
        initTopicTree();
    }
    
    /**
     * Initializes the topic tree, if there is one associated with this view.
     */
    private void initTopicTree() {
        DefaultMutableTreeNode topicTree = getDataAsTree();
        
        if (topicTree != null) {
            initTopicTree(rootNode, topicTree.getRoot());
        }
        
    }
    
    /**
     * Duplicates JavaHelp topic tree into HelpTopicNode-based tree.
     * 
     * @param htnParent Current parent for HelpTopicNode-based tree.
     * @param ttnParent Current parent for JavaHelp TreeNode-based tree.
     */
    private void initTopicTree(HelpTopicNode htnParent, TreeNode ttnParent) {
        
        for (int i = 0; i < ttnParent.getChildCount(); i++) {
            TreeNode ttnChild = ttnParent.getChildAt(i);
            HelpTopic ht = getTopic(ttnChild);
            HelpTopicNode htnChild = new HelpTopicNode(ht);
            htnParent.addChild(htnChild);
            initTopicTree(htnChild, ttnChild);
        }
        
    }
    
    /**
     * Retrieves the help topic associated with the given node. Note that this also maintains a map
     * of topic id --> topic mappings in the parent help set.
     * 
     * @param node Tree node.
     * @return A help topic instance.
     */
    protected HelpTopic getTopic(TreeNode node) {
        try {
            DefaultMutableTreeNode nd = (DefaultMutableTreeNode) node;
            TreeItem item = (TreeItem) nd.getUserObject();
            ID id = item.getID();
            HelpTopic topic = new HelpTopic(id == null ? null : id.getURL(), item.getName(), view.getHelpSet().getTitle());
            
            if (id != null && view.getHelpSet().getKeyData("topics", id.id) == null) {
                view.getHelpSet().setKeyData("topics", id.id, topic);
            }
            
            return topic;
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    /**
     * Invokes the "getDataAsTree" method on the underlying view, if such a method exists.
     * 
     * @return The view's topic tree, or null if none.
     */
    protected DefaultMutableTreeNode getDataAsTree() {
        try {
            return (DefaultMutableTreeNode) MethodUtils.invokeMethod(view, "getDataAsTree", null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns the root node of the topic tree.
     */
    @Override
    public HelpTopicNode getTopicTree() {
        return rootNode;
    }
    
    /**
     * Returns the view type.
     */
    @Override
    public HelpViewType getViewType() {
        return viewType;
    }
    
}
