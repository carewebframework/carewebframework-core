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
package org.carewebframework.shell.property;

import java.util.Properties;

import org.carewebframework.api.property.IPropertyProvider;

/**
 * A simple property map that can be used to pass property values to a plugin during
 * deserialization.
 */
public class PropertyBag extends Properties implements IPropertyProvider {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor that can take a variable length list of name=value entries.
     * 
     * @param properties A list of name=value pairs.
     */
    public PropertyBag(String... properties) {
        super();
        
        for (String property : properties) {
            String[] nv = property.split("\\=", 2);
            setProperty(nv[0], nv.length == 1 ? null : nv[1]);
        }
    }
    
    /**
     * Returns true if the property exists.
     * 
     * @param key The property name.
     * @return True if the property exists.
     */
    @Override
    public boolean hasProperty(String key) {
        return containsKey(key);
    }
}
