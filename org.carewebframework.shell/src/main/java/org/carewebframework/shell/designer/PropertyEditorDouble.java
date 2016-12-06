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

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.web.component.Doublebox;
import org.carewebframework.web.event.ChangeEvent;

/**
 * Editor for double values.
 */
public class PropertyEditorDouble extends PropertyEditorBase {
    
    private final Doublebox doublebox;
    
    public PropertyEditorDouble() {
        super(new Doublebox());
        doublebox = (Doublebox) component;
    }
    
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        doublebox.addEventForward(ChangeEvent.TYPE, propGrid, null);
        doublebox.setMinValue(propInfo.getConfigValueDouble("min", null));
        doublebox.setMaxValue(propInfo.getConfigValueDouble("max", null));
    }
    
    @Override
    protected String getValue() {
        return Double.toString(doublebox.getValue());
    }
    
    @Override
    protected void setValue(Object value) {
        doublebox.setValue((Double) value);
        updateValue();
    }
}
