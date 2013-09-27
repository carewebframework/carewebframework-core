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

import java.util.List;

import org.zkoss.zul.impl.XulElement;

/**
 * Wonder bar component that serves as a parent for all search results.
 */
public class WonderbarItems extends XulElement {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-wonderbar-items" : _zclass;
    }
    
    @SuppressWarnings("unchecked")
    public List<WonderbarAbstractItem> getItems() {
        return (List<WonderbarAbstractItem>) (List<?>) getChildren();
    }
    
    public void clear() {
        getChildren().clear();
    }
}
