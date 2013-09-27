/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.xml;

import java.util.Collection;

import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTreeModel extends DefaultTreeModel<Node> {
    
    private static final long serialVersionUID = 1L;
    
    public static class XMLTreeNode extends DefaultTreeNode<Node> {
        
        private static final long serialVersionUID = 1L;
        
        public static XMLTreeNode create(Node node) {
            return (node != null && node.hasChildNodes() ? new XMLTreeNode(node, null) : new XMLTreeNode(node));
        }
        
        private XMLTreeNode(Node node) {
            super(node);
        }
        
        private XMLTreeNode(Node node, Collection<XMLTreeNode> children) {
            super(node, children, true);
        }
        
        public XMLTreeNode addChild(Node node) {
            XMLTreeNode treeNode = create(node);
            add(treeNode);
            return treeNode;
        }
    }
    
    private final boolean tagPairs;
    
    public XMLTreeModel(Document document, boolean tagPairs) {
        super(XMLTreeNode.create(document));
        this.tagPairs = tagPairs;
        buildModel((XMLTreeNode) getRoot(), document.getDocumentElement());
    }
    
    /**
     * Loads a tree from an XML document.
     * 
     * @param parent Parent tree node.
     * @param node Current document node.
     */
    private void buildModel(XMLTreeNode parent, Node node) {
        XMLTreeNode treeNode = parent.addChild(node);
        
        if (node.hasChildNodes()) {
            if (tagPairs) {
                parent.addChild(node.cloneNode(false));
            }
            
            NodeList children = node.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                buildModel(treeNode, children.item(i));
            }
        }
    }
    
}
