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

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.TreeUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.Event;
import org.w3c.dom.Document;

/**
 * Controller for XMLViewer.
 */
public class XMLViewerController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Renderer for tree.
     */
    private static final XMLViewerRenderer renderer = new XMLViewerRenderer();
    
    private Tree tree;
    
    private Textbox textbox;
    
    private Label lblNotFound;
    
    private Treeitem lastItem;
    
    /**
     * Sets the renderer.
     * 
     * @throws Exception Unspecified exception.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        tree.setItemRenderer(renderer);
    }
    
    /**
     * Perform a search.
     */
    public void onSearch() {
        String text = textbox.getValue();
        
        if (text != null && !text.isEmpty()) {
            lastItem = lastItem == null ? TreeUtil.search(tree, text, renderer.treeitemSearch)
                    : TreeUtil.search(lastItem, text, renderer.treeitemSearch);
            lblNotFound.setVisible(lastItem == null);
            
            if (lastItem != null) {
                TreeUtil.makeVisible(lastItem);
                lastItem.setSelected(true);
            } else {
                lastItem = tree.getSelectedItem();
            }
            
            textbox.setFocus(true);
        }
    }
    
    /**
     * XML document is the data associated with the onModal event.
     * 
     * @param event The onModal event.
     */
    public void onModal(Event event) {
        Document document = (Document) event.getData();
        tree.setModel(new XMLTreeModel(document, true));
    }
    
    /**
     * Selecting an item sets it as the starting point for a search.
     */
    public void onSelect$tree() {
        lastItem = tree.getSelectedItem();
    }
}
