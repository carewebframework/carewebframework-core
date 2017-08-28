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

import org.fujion.common.DateUtil;
import org.carewebframework.shell.property.PropertyInfo;
import org.fujion.component.Datebox;

/**
 * Editor for dates.
 */
public class PropertyEditorDate extends PropertyEditorBase<Datebox> {

    public PropertyEditorDate() {
        super(new Datebox());
    }

    @Override
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        //TODO: component.setConstraint(propInfo.getConfigValue("constraint"));
    }

    @Override
    protected String getValue() {
        return DateUtil.formatDate(editor.getValue());
    }

    @Override
    protected void setValue(Object value) {
        editor.setValue((Date) value);
        updateValue();
    }

}
