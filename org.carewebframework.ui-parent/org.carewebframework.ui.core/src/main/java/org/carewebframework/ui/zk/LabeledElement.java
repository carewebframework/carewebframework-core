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
    
    @Override
    protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
        super.renderProperties(renderer);
        render(renderer, "align", _align);
        render(renderer, "position", _position);
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-labeledelement" : _zclass;
    }
    
    public String getPosition() {
        return _position;
    }
    
    public void setPosition(String position) {
        position = position == null || position.isEmpty() ? "left" : position;
        
        if (!"left".equals(position) && !"right".equals(position) && !"top".equals(position) && !"bottom".equals(position)) {
            throw new WrongValueException(position);
        }
        
        _position = position;
        smartUpdate("position", _position);
    }
    
    public String getAlign() {
        return _align;
    }
    
    public void setAlign(String align) {
        align = align == null || align.isEmpty() ? "start" : align;
        
        if (!"start".equals(align) && !"center".equals(align) && !"end".equals(align)) {
            throw new WrongValueException(align);
        }
        
        _align = align;
        smartUpdate("align", _align);
    }
    
}
