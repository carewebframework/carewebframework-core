/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.wonderbar.test;

import org.carewebframework.ui.wonderbar.IWonderbarItemRenderer;
import org.carewebframework.ui.wonderbar.WonderbarItem;

/**
 * Simple test provider
 */
public class TestItemRenderer implements IWonderbarItemRenderer<TestSearchItem> {
    
    @Override
    public void render(WonderbarItem item, TestSearchItem data, int index) {
        item.setLabel(data.label);
        item.setValue(data.value);
        item.setCategory(data.category);
        item.setChoiceNumber(data.choice);
        item.setData(item);
    }
    
}
