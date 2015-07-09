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

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.impl.LabelElement;

/**
 * Component to facilitate adding a label to another component or component group.
 */
public class LabeledElement extends LabelElement {
    
    private static final long serialVersionUID = 1L;
    
    private String _align = "start";
    
    private String _position = "left";
    
    private String _labelStyle;
    
    @Override
    protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
        super.renderProperties(renderer);
        render(renderer, "align", _align);
        render(renderer, "position", _position);
        render(renderer, "labelStyle", _labelStyle);
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-labeledelement" : _zclass;
    }
    
    /**
     * Returns the position of the label relative to the contained elements. Defaults to 'left'.
     * 
     * @return May be one of: left, right, top, or bottom.
     */
    public String getPosition() {
        return _position;
    }
    
    /**
     * Sets the position of the label relative to the contained elements.
     * 
     * @param position May be one of: left, right, top, or bottom.
     */
    public void setPosition(String position) {
        position = position == null || position.isEmpty() ? "left" : position;
        
        if (!"left".equals(position) && !"right".equals(position) && !"top".equals(position) && !"bottom".equals(position)) {
            throw new WrongValueException(position);
        }
        
        _position = position;
        smartUpdate("position", _position);
    }
    
    /**
     * Returns the alignment of the label. Defaults to 'start'.
     * 
     * @return May be one of start, center, end.
     */
    public String getAlign() {
        return _align;
    }
    
    /**
     * Sets the alignment of the label.
     * 
     * @param align May be one of: start, center, end.
     */
    public void setAlign(String align) {
        align = align == null || align.isEmpty() ? "start" : align;
        
        if (!"start".equals(align) && !"center".equals(align) && !"end".equals(align)) {
            throw new WrongValueException(align);
        }
        
        _align = align;
        smartUpdate("align", _align);
    }
    
    /**
     * Returns the style(s) associated with the label.
     * 
     * @return The label style(s).
     */
    public String getLabelStyle() {
        return _labelStyle;
    }
    
    /**
     * Sets the style(s) of the label.
     * 
     * @param labelStyle The label style(s).
     */
    public void setLabelStyle(String labelStyle) {
        _labelStyle = labelStyle;
        smartUpdate("labelStyle", _labelStyle);
    }
    
}
