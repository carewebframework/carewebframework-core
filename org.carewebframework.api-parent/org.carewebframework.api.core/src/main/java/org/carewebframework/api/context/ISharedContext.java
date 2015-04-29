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

/**
 * Every managed context must implement this interface to permit access to the wrapped domain
 * object.
 * 
 * @param <DomainClass> This represents the domain class that is wrapped by this managed context.
 */
public interface ISharedContext<DomainClass> {
    
    /**
     * Returns the underlying domain object associated with the current or pending context.
     * 
     * @param pending If true, the domain object associated with the pending context is returned. If
     *            false, the domain object associated with the current context is returned.
     * @return Domain object in the specified context.
     */
    DomainClass getContextObject(boolean pending);
    
    /**
     * Sets the specified domain object into the pending context and invokes the context change
     * sequence.
     * 
     * @param newContextObject Domain object
     * @throws ContextException Context exception.
     */
    void requestContextChange(DomainClass newContextObject) throws ContextException;
}
