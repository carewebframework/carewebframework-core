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
    
    private long id;
    
    public DomainObject() {
    }
    
    public DomainObject(long id) {
        this.id = id;
    }
    
    @Override
    public long getDomainId() {
        return id;
    }
    
    @Override
    public void setDomainId(long id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass().equals(getClass()) && ((DomainObject) object).id == id;
    }
    
    @Override
    public Object getProxiedObject() {
        return this;
    }
}
