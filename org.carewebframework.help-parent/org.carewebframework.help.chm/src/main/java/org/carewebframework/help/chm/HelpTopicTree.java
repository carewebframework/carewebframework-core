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

import org.carewebframework.common.XMLUtil;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpTopicNode;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Represents a hierarchy of topics for an index, TOC, etc.
 */
public class HelpTopicTree {
    
    private final HelpSet_CHMHelp helpSet;
    
    private final HelpTopicNode rootNode;
    
    public HelpTopicTree(HelpSet_CHMHelp helpSet, String file) throws Exception {
        this.helpSet = helpSet;
        this.rootNode = new HelpTopicNode(new HelpTopic(helpSet.getName()));
        Document doc = XMLUtil.parseXMLFromStream(helpSet.openStream(file));
        Node root = doc.getFirstChild();
        
        if (root != null) {
            initTopicTree(root, rootNode);
        }
    }
    
    public boolean isEmpty() {
        return rootNode.getChildren().isEmpty();
    }
    
    public HelpTopicNode getRootNode() {
        return rootNode;
    }
    
    private void initTopicTree(Node parentNode, HelpTopicNode parentTopic) throws Exception {
        Node child = parentNode.getFirstChild();
        
        while (child != null) {
            if ("topic".equals(child.getNodeName())) {
                NamedNodeMap attrs = child.getAttributes();
                String id = getAttributeValue("id", attrs);
                String url = getAttributeValue("url", attrs);
                String label = getAttributeValue("label", attrs);
                HelpTopic topic = new HelpTopic(url == null ? null : helpSet.getURL(url), label, helpSet.getName());
                HelpTopicNode htn = new HelpTopicNode(topic, id);
                parentTopic.addChild(htn);
                initTopicTree(child, htn);
                
                if (id != null) {
                    helpSet.registerTopic(id, topic);
                }
            }
            child = child.getNextSibling();
        }
    }
    
    private String getAttributeValue(String attrName, NamedNodeMap attrs) {
        Node node = attrs == null ? null : attrs.getNamedItem(attrName);
        return node == null ? null : node.getNodeValue();
    }
    
}
