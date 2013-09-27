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

/**
 * A wonder bar separator. A separator is a non-selectable wonder bar item that presents a visual
 * separation between items.
 */
public class WonderbarSeparator extends WonderbarAbstractItem {
    
    private static final long serialVersionUID = 1L;
    
    public WonderbarSeparator() {
        super();
    }
    
    @Override
    public String getWidgetClass() {
        return "wonderbar.ext.WonderbarSeparator";
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-wonderbar-separator" : _zclass;
    }
    
}
