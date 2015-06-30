/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

/**
 * Base tree item renderer.
 * 
 * @param <T> Data type of tree item-associated object.
 */
public abstract class AbstractTreeitemRenderer<T> extends AbstractRenderer implements TreeitemRenderer<T> {
    
    /**
     * No args Constructor
     */
    public AbstractTreeitemRenderer() {
        super();
    }
    
    /**
     * @param itemStyle Style to be applied to each tree item.
     * @param cellStyle Style to be applied to each cell.
     */
    public AbstractTreeitemRenderer(String itemStyle, String cellStyle) {
        super(itemStyle, cellStyle);
    }
    
    @Override
    public void render(Treeitem item, T object, int index) throws Exception {
        item.setValue(object);
        item.setStyle(compStyle);
        renderItem(item, object);
    }
    
    protected abstract void renderItem(Treeitem item, T object);
    
    /**
     * Creates a tree cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the tree cell.
     * @param value Value to be used as label text.
     * @return The newly created list cell.
     */
    public Treecell createCell(Component parent, Object value) {
        return createCell(parent, value, null);
    }
    
    /**
     * Creates a tree cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the tree cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created tree cell.
     */
    public Treecell createCell(Component parent, Object value, String prefix) {
        return createCell(parent, value, prefix, null);
    }
    
    /**
     * Creates a tree cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the tree cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created tree cell.
     */
    public Treecell createCell(Component parent, Object value, String prefix, String style) {
        return createCell(parent, value, prefix, style, null, Treecell.class);
    }
    
}
