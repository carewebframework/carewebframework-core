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

import java.util.ArrayList;
import java.util.List;

import org.fujion.model.IListModel;
import org.fujion.model.NestedModel;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTreeModel extends NestedModel<Node> {
    
    private static List<Node> getChildNodes(Node root) {
        NodeList children = root.getChildNodes();
        List<Node> list = new ArrayList<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                continue;
            }
            
            list.add(child);
            
            if (child.hasChildNodes()) {
                list.add(child.cloneNode(false));
            }
        }
        
        return list;
    }
    
    public XMLTreeModel(Node root) {
        super(getChildNodes(root));
    }
    
    @Override
    public IListModel<Node> getChildren(Node parent) {
        return new XMLTreeModel(parent);
    }
    
}
