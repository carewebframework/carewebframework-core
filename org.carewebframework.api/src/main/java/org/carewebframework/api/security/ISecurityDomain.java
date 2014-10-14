/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.security;

import java.io.Serializable;

/**
 * Interface for a security domain. A security domain is responsible for providing user
 * authentication and authorization.
 */
public interface ISecurityDomain extends Serializable {
    
    /**
     * Returns the domain's name.
     * 
     * @return Domain's display name.
     */
    String getName();
    
    /**
     * Returns the logical identifier for the security domain.
     * 
     * @return Security domain logical identifier.
     */
    String getLogicalId();
    
    /**
     * Returns a named attribute for the security domain.
     * 
     * @param name The attribute name.
     * @return The attribute value, or null if not found.
     */
    String getAttribute(String name);
    
    /**
     * Returns the native security domain object if this is a proxy.
     * 
     * @return The native security domain object.
     */
    Object getNativeSecurityDomain();
}
