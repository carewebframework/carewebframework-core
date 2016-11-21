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

import org.carewebframework.common.XMLUtil;
import org.carewebframework.common.XMLUtil.TagFormat;
import org.carewebframework.ui.xml.XMLTreeModel.XMLTreeNode;
import org.carewebframework.ui.zk.TreeUtil.ITreenodeSearch;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.model.IComponentRenderer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Renderer for xml tree.
 */
class XMLViewerRenderer implements IComponentRenderer<Treenode, XMLTreeNode> {
    
    /**
     * Value associated with each tree item.
     */
    private class TreenodeValue {
        
        private Cell cell;
        
        private Label closingTag;
        
        private String text = "";
        
    }
    
    /**
     * Open event listener for tree items in XML viewer.
     */
    private final EventListener<OpenEvent> nodeListener = new EventListener<OpenEvent>() {
        
        @Override
        public void onEvent(OpenEvent event) throws Exception {
            Treenode item = (Treenode) event.getTarget();
            boolean open = event.isOpen();
            TreenodeValue itemValue = (TreenodeValue) item.getValue();
            itemValue.closingTag.setVisible(!open);
            Treenode sib = (Treenode) item.getNextSibling();
            
            if (sib != null) {
                sib.setVisible(item.isOpen());
            }
        }
        
    };
    
    /**
     * Search logic for tree.
     */
    protected final ITreenodeSearch treeitemSearch = new ITreenodeSearch() {
        
        @Override
        public boolean isMatch(Treenode item, String text) {
            String label = ((TreenodeValue) item.getData()).text;
            return label != null && label.contains(text.toLowerCase());
        }
        
    };
    
    @Override
    public Treenode render(XMLTreeNode data) {
        Treenode item = new Treenode();
        Node node = data.getData();
        item.setLabel(null);
        TreenodeValue itemValue = new TreenodeValue();
        item.setData(itemValue);
        itemValue.cell = (Treecell) item.getTreerow().getFirstChild();
        
        if (node.getNodeType() == Node.TEXT_NODE) {
            addLabel(itemValue, node.getNodeValue(), XMLConstants.STYLE_CONTENT);
            return item;
        }
        
        if (node.getParentNode() == null) { // Closing tag
            addLabel(itemValue, XMLUtil.formatNodeName(node, TagFormat.CLOSING), XMLConstants.STYLE_TAG);
            return item;
        }
        
        boolean leaf = !node.hasChildNodes();
        addLabel(itemValue, "<" + node.getNodeName(), XMLConstants.STYLE_TAG);
        
        NamedNodeMap attrs = node.getAttributes();
        
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            addLabel(itemValue, " " + attr.getNodeName(), XMLConstants.STYLE_ATTR_NAME);
            addLabel(itemValue, "='", null);
            addLabel(itemValue, attr.getNodeValue(), XMLConstants.STYLE_ATTR_VALUE);
            addLabel(itemValue, "'", null);
        }
        
        addLabel(itemValue, (leaf ? " />" : ">"), XMLConstants.STYLE_TAG);
        
        if (!leaf) {
            Label label = addLabel(itemValue, XMLUtil.formatNodeName(node, TagFormat.CLOSING), XMLConstants.STYLE_TAG);
            itemValue.closingTag = label;
            label.setVisible(false);
            item.setOpen(true);
            item.addEventListener(Events.ON_OPEN, nodeListener);
        }
    }
    
    private Label addLabel(TreenodeValue itemValue, String text, String sclass) {
        Label label = new Label(text);
        label.addClass(sclass);
        label.setParent(itemValue.cell);
        itemValue.text += text.toLowerCase();
        return label;
    }
    
}
