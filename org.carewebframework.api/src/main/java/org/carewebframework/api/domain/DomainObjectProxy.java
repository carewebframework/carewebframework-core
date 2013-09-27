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
 * Abstract base class for wrapping an existing domain object. The wrapped object may be directly
 * accessed via the getProxiedObject method. This class is useful for exposing domain objects in a
 * standard way such that plug-ins may be written in an implementation-independent fashion.
 * 
 * @param <T> The class of the wrapped domain object.
 */
public abstract class DomainObjectProxy<T> implements IDomainObject, Serializable {
    
    private static final long serialVersionUID = 1L;

    protected T proxiedObject;
    
    /**
     * @param proxiedObject T
     */
    public DomainObjectProxy(T proxiedObject) {
        this.proxiedObject = proxiedObject;
    }
    
    /**
     * @see org.carewebframework.api.domain.IDomainObject#getProxiedObject()
     */
    @Override
    public T getProxiedObject() {
        return proxiedObject;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass().equals(getClass())
                && ((DomainObjectProxy<?>) object).getDomainId() == getDomainId();
    }
    
}
