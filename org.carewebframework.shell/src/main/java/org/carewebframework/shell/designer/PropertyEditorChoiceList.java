/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;

/**
 * Editor for choice lists.
 */
public class PropertyEditorChoiceList extends PropertyEditorList {
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        
        /* If a value has two parts separated by a colon, the first is assumed to be the exteneral
         * representation and the second is the internal representation.  Otherwise, the internal and
         * external are assumed to be the same.
         */
        String[] values = propInfo.getConfigValueArray("values");
        
        if (values != null) {
            for (String value : values) {
                appendItem(value);
            }
        }
    }
}
