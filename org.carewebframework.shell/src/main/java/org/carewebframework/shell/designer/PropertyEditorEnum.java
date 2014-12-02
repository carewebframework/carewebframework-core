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
 * Editor for enumerable types (enums and iterables). The editor expects one of two named config
 * parameters: class or bean. A class may be an enum or an iterable. A bean is the id of a bean that
 * implements an iterable.
 */
public class PropertyEditorEnum extends PropertyEditorList {
    
    /**
     * Initialize the list, based on the configuration data which can specify an enumeration class,
     * an iterable class, or the id of an iterable bean.
     */
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        
        Iterable<?> iter = (Iterable<?>) propInfo.getPropertyType().getSerializer();
        
        for (Object value : iter) {
            appendItem(value.toString(), value);
        }
        
    }
}
