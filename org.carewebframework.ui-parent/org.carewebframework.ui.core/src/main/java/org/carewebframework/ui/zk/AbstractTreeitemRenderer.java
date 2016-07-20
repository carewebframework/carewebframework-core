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
