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

import java.io.Serializable;

/**
 * Abstract base class for implementing domain objects. This class is useful for implementing a
 * domain object from scratch. To wrap an existing domain object, use DomainObjectProxy.
 */
public abstract class DomainObject implements Serializable, IDomainObject {
    
    private static final long serialVersionUID = 1L;
    
    private String logicalId;
    
    private String universalId;
    
    public DomainObject() {
    }
    
    public DomainObject(DomainObject src) {
        logicalId = src.logicalId;
    }
    
    public DomainObject(String logicalId, String universalId) {
        this.logicalId = logicalId;
        this.universalId = universalId;
    }
    
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    protected void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }
    
    @Override
    public String getUniversalId() {
        return universalId;
    }
    
    protected void setUniversalId(String universalId) {
        this.universalId = universalId;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof DomainObject) {
            DomainObject tgt = (DomainObject) object;
            return getClass() == tgt.getClass() && getUniversalId() != null && tgt.getUniversalId() != null
                    && getUniversalId().equals(tgt.getUniversalId());
        }
        
        return false;
    }
    
}
