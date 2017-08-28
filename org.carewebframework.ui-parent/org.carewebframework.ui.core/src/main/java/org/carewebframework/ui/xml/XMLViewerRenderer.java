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

import org.fujion.common.XMLUtil;
import org.fujion.common.XMLUtil.TagFormat;
import org.fujion.component.Treenode;
import org.fujion.event.IEventListener;
import org.fujion.model.IComponentRenderer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Renderer for xml tree.
 */
class XMLViewerRenderer implements IComponentRenderer<Treenode, Node> {
    
    /**
     * Open event listener for tree items in XML viewer.
     */
    private final IEventListener nodeListener = event -> {
        Treenode tnode = (Treenode) event.getTarget();
        boolean open = !tnode.isCollapsed();
        Treenode sib = (Treenode) tnode.getNextSibling();

        if (sib != null) {
            sib.setVisible(open);
        }
    };
    
    @Override
    public Treenode render(Node node) {
        Treenode tnode = new Treenode();
        
        if (node.getNodeType() == Node.TEXT_NODE) {
            setLabel(tnode, node.getNodeValue(), XMLConstants.STYLE_CONTENT);
            return tnode;
        }
        
        if (node.getNodeType() == Node.COMMENT_NODE) {
            setLabel(tnode, "<!--" + node.getNodeValue() + "-->", XMLConstants.STYLE_COMMENT);
            return tnode;
        }

        if (node.getParentNode() == null) { // Closing tag
            setLabel(tnode, XMLUtil.formatNodeName(node, TagFormat.CLOSING), XMLConstants.STYLE_TAG);
            return tnode;
        }
        
        boolean leaf = !node.hasChildNodes();
        String label = "<" + node.getNodeName();
        NamedNodeMap attrs = node.getAttributes();
        
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                label += " " + attr.getNodeName() + "='" + attr.getNodeValue() + "'";
            }
        }
        
        setLabel(tnode, label + (leaf ? " />" : ">"), XMLConstants.STYLE_TAG);
        tnode.addEventListener("toggle", nodeListener);
        return tnode;
    }
    
    private void setLabel(Treenode node, String text, String sclass) {
        node.setLabel(text);
        node.addClass(sclass);
    }
    
}
