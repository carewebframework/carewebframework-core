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
