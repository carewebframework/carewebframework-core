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

import java.util.Date;

import org.carewebframework.common.DateUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;

/**
 * Editor for dates.
 */
public class PropertyEditorDate extends PropertyEditorBase {
    
    private final Datebox datebox;
    
    public PropertyEditorDate() {
        super(new Datebox());
        datebox = (Datebox) component;
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        datebox.setConstraint(propInfo.getConfigValue("constraint"));
        datebox.addForward(Events.ON_CHANGING, propGrid, Events.ON_CHANGE);
    }
    
    @Override
    protected String getValue() {
        return DateUtil.formatDate(datebox.getValue());
    }
    
    @Override
    protected void setValue(Object value) {
        datebox.setValue((Date) value);
        updateValue();
    }
}
