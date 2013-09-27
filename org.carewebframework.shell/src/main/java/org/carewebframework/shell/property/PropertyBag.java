/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.property;

import java.util.Properties;

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
