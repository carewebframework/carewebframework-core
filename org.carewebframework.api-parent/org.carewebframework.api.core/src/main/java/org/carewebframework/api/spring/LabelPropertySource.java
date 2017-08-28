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
package org.carewebframework.api.spring;

import org.fujion.common.StrUtil;
import org.springframework.core.env.PropertySource;

/**
 * Allows label identifiers to be resolved within Spring.
 */
public class LabelPropertySource extends PropertySource<Object> {
    
    private static final String LABEL_PREFIX = "@msg.";
    
    public LabelPropertySource() {
        super("Labels");
    }
    
    /**
     * Label names must be prefixed with "@msg." to be recognized as such.
     */
    @Override
    public String getProperty(String name) {
        return name.startsWith(LABEL_PREFIX) ? StrUtil.getLabel(name.substring(LABEL_PREFIX.length())) : null;
    }
    
}
