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
 * A wonder bar group item. A group item provides a non-selectable header for a related group of
 * wonder bar items.
 */
public class WonderbarGroup extends WonderbarAbstractItem {
    
    private static final long serialVersionUID = 1L;
    
    public WonderbarGroup() {
        super();
    }
    
    public WonderbarGroup(String label) {
        super(label);
    }
    
    @Override
    public String getWidgetClass() {
        return "wonderbar.ext.WonderbarGroup";
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-wonderbar-group" : _zclass;
    }
    
    @Override
    public String getLabel() {
        return super.getLabel();
    }
    
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }
    
}
