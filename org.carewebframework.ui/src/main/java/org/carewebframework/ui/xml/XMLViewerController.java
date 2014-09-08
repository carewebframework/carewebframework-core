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

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.TreeUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;

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
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        tree.setItemRenderer(renderer);
    }
    
    /**
     * Perform a search.
     */
    public void onSearch() {
        String text = textbox.getValue();
        
        if (text != null && !text.isEmpty()) {
            lastItem = lastItem == null ? TreeUtil.search(tree, text, renderer.treeitemSearch) : TreeUtil.search(lastItem,
                text, renderer.treeitemSearch);
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
        Clients.resize(tree);
    }
    
    /**
     * Selecting an item sets it as the starting point for a search.
     */
    public void onSelect$tree() {
        lastItem = tree.getSelectedItem();
    }
}
