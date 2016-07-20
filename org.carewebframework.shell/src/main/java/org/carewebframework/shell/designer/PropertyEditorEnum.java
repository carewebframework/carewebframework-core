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
