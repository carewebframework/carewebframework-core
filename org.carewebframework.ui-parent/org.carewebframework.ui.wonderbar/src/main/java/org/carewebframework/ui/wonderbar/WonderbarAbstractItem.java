/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.wonderbar;

import java.io.IOException;

import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.impl.XulElement;

/**
 * Base class for wonder bar item and group.
 */
public abstract class WonderbarAbstractItem extends XulElement {
    
    private static final long serialVersionUID = 1L;
    
    private String value;
    
    private String label;
    
    private Object data;
    
    protected WonderbarAbstractItem() {
    }
    
    protected WonderbarAbstractItem(String label) {
        this(label, null);
    }
    
    protected WonderbarAbstractItem(String label, String value) {
        this(label, value, null);
    }
    
    protected WonderbarAbstractItem(String label, String value, Object data) {
        this();
        this.label = label;
        this.value = value;
        this.data = data;
    }
    
    @Override
    public void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        renderer.render("label", label);
        renderer.render("value", value);
    }
    
    @Override
    public String toString() {
        return value == null ? label : value;
    }
    
    /**
     * Returns the value property. When present, the contents of value, rather than that of the
     * label, is placed in the input box when the associated item is selected.
     * 
     * @return The value property
     */
    protected String getValue() {
        return value;
    }
    
    /**
     * Sets the value property. When present, the contents of value, rather than that of the label,
     * is placed in the input box when the associated item is selected.
     * 
     * @param value The value property
     */
    protected void setValue(String value) {
        this.value = value;
        smartUpdate("value", value);
    }
    
    /**
     * Returns the label text. The label text determines what is displayed in the choice list.
     * Unless the value property is set, the label text is place in the input box when the
     * associated item is selected.
     * 
     * @return The label text.
     */
    protected String getLabel() {
        return label;
    }
    
    /**
     * Sets the label text. The label text determines what is displayed in the choice list. Unless
     * the value property is set, the label text is place in the input box when the associated item
     * is selected.
     * 
     * @param label The label text.
     */
    protected void setLabel(String label) {
        this.label = label;
        smartUpdate("label", label);
    }
    
    /**
     * Returns the data associated with the item.
     * 
     * @return Data associated with the item.
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Set the data associated with the item.
     * 
     * @param data Data associated with the item.
     */
    public void setData(Object data) {
        this.data = data;
    }
    
}
