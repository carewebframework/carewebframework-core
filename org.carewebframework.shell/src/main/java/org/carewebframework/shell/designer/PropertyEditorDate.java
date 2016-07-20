/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
