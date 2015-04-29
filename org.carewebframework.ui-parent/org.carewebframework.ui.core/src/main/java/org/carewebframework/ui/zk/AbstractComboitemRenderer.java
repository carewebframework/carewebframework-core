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

import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;

/**
 * Base row renderer.
 * 
 * @param <T> Class of rendered object.
 */
public abstract class AbstractComboitemRenderer<T> extends AbstractRenderer implements ComboitemRenderer<T> {
    
    /**
     * No args Constructor
     */
    public AbstractComboitemRenderer() {
        super();
    }
    
    @Override
    public final void render(final Comboitem item, final T object, int index) throws Exception {
        item.setValue(object);
        renderItem(item, object);
    }
    
    protected abstract void renderItem(Comboitem item, T object);
    
}
