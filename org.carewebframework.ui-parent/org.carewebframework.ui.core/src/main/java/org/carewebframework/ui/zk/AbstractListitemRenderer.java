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

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Listitem;

/**
 * Base list item renderer.
 * 
 * @param <T> Data type of list item-associated object.
 * @param <G> Data type of group-associated object.
 */
public abstract class AbstractListitemRenderer<T, G> extends AbstractRenderer implements ListitemRenderer<T> {
    
    /**
     * No args Constructor
     */
    public AbstractListitemRenderer() {
        super();
    }
    
    /**
     * @param itemStyle Style to be applied to each list item.
     * @param cellStyle Style to be applied to each cell.
     */
    public AbstractListitemRenderer(String itemStyle, String cellStyle) {
        super(itemStyle, cellStyle);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void render(Listitem item, Object object, int index) throws Exception {
        item.setValue(object);
        
        if (item instanceof Listgroup) {
            renderGroup((Listgroup) item, (G) object);
        } else {
            item.setStyle(compStyle);
            renderItem(item, (T) object);
        }
    }
    
    protected abstract void renderItem(Listitem item, T object);
    
    protected void renderGroup(Listgroup group, G object) {
        group.setLabel(object.toString());
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent BaseComponent that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @return The newly created list cell.
     */
    public Listcell createCell(BaseComponent parent, Object value) {
        return createCell(parent, value, null);
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent BaseComponent that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created list cell.
     */
    public Listcell createCell(BaseComponent parent, Object value, String prefix) {
        return createCell(parent, value, prefix, null);
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent BaseComponent that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created list cell.
     */
    public Listcell createCell(BaseComponent parent, Object value, String prefix, String style) {
        return createCell(parent, value, prefix, style, null, Listcell.class);
    }
    
}
