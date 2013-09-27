/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.domain;

import org.carewebframework.common.JSONUtil;

/**
 * Represents an identifier (the id) within a specific coding system (the sysId).
 */
public class EntityIdentifier {
    
    static {
        JSONUtil.registerAlias("EntityIdentifier", EntityIdentifier.class);
    }
    
    private final String id;
    
    private final String sysId;
    
    public EntityIdentifier(String id, String sysId) {
        this.id = id;
        this.sysId = sysId;
    }
    
    public String getId() {
        return id;
    }
    
    public String getSysId() {
        return sysId;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EntityIdentifier)) {
            return false;
        }
        
        EntityIdentifier ei = (EntityIdentifier) object;
        return ei.id.equals(id) && ei.sysId.equals(sysId);
    }
}
