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
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listgroup;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Base row renderer.
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
    public final void render(final Listitem item, final Object object, int index) throws Exception {
        item.setValue(object);
        
        if (item instanceof Listgroup) {
            renderGroup((Listgroup) item, (G) object);
        }
        item.setStyle(compStyle);
        renderItem(item, (T) object);
    }
    
    protected abstract void renderItem(Listitem item, T object);
    
    protected void renderGroup(Listgroup group, G object) {
        group.setLabel(object.toString());
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @return The newly created list cell.
     */
    protected Listcell createCell(Component parent, Object value) {
        return createCell(parent, value, null);
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created list cell.
     */
    protected Listcell createCell(Component parent, Object value, String prefix) {
        return createCell(parent, value, prefix, null);
    }
    
    /**
     * Creates a list cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the list cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created list cell.
     */
    protected Listcell createCell(Component parent, Object value, String prefix, String style) {
        return createCell(parent, value, prefix, style, null, Listcell.class);
    }
    
}
