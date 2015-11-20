/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.chm;

import org.carewebframework.help.HelpTopicNode;
import org.carewebframework.help.viewer.HelpViewType;
import org.carewebframework.help.viewer.IHelpView;

/**
 * IHelpView implementation for HTML Help navigator views.
 */
public class HelpView implements IHelpView {
    
    private final HelpViewType viewType;
    
    private final HelpTopicNode topics;
    
    /**
     * Create help view for given navigator view and view type.
     * 
     * @param topicTree The associated topic tree.
     * @param viewType View type.
     * @throws Exception An exception.
     */
    public HelpView(HelpTopicTree topicTree, HelpViewType viewType) throws Exception {
        this.viewType = viewType;
        
        if (viewType == HelpViewType.TOC) {
            topics = new HelpTopicNode(null);
            topics.addChild(topicTree.getRootNode());
        } else {
            topics = topicTree.getRootNode();
        }
    }
    
    /**
     * Returns the root node of the topic tree.
     */
    @Override
    public HelpTopicNode getTopicTree() {
        return topics;
    }
    
    /**
     * Returns the view type.
     */
    @Override
    public HelpViewType getViewType() {
        return viewType;
    }
    
}
