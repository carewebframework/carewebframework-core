/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.context;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.domain.Name;

/**
 * CCOW serializer for name component.
 */
public class NameSerializer implements IContextSerializer {
    
    private static final String NAME_DELIM = "^";
    
    @Override
    public String serialize(Object value) {
        Name name = (Name) value;
        return StringUtils.defaultString(name.getLastName())
                + NAME_DELIM
                + StringUtils.defaultString((name.getFirstName()) + NAME_DELIM
                        + StringUtils.defaultString(name.getMiddleName()));
    }
    
    @Override
    public Name deserialize(String value) {
        String pcs[] = value.split("\\" + NAME_DELIM);
        Name result = new Name();
        
        if (pcs.length > 0) {
            result.setLastName(pcs[0]);
        }
        
        if (pcs.length > 1) {
            result.setFirstName(pcs[1]);
        }
        
        if (pcs.length > 2) {
            result.setMiddleName(pcs[2]);
        }
        
        return result;
    }
    
    @Override
    public Class<?> getType() {
        return Name.class;
    }
}
