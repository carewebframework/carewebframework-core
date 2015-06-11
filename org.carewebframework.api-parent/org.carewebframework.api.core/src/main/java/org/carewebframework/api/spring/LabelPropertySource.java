/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.carewebframework.common.StrUtil;

import org.springframework.core.env.PropertySource;

/**
 * Allows label identifiers to be resolved within Spring.
 */
public class LabelPropertySource extends PropertySource<Object> {
    
    private static final String LABEL_PREFIX = "labels.";
    
    public LabelPropertySource() {
        super("Labels");
    }
    
    /**
     * Label names must be prefixed with "labels." to be recognized as such.
     */
    @Override
    public String getProperty(String name) {
        return name.startsWith(LABEL_PREFIX) ? StrUtil.getLabel(name.substring(LABEL_PREFIX.length())) : null;
    }
    
}
