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

import org.carewebframework.api.security.ISecurityDomain;

/**
 * Interface for a user.
 */
public interface IUser extends Serializable {
    
    /**
     * Returns the logical identifier for the user.
     * 
     * @return User logical identifier.
     */
    String getLogicalId();
    
    /**
     * Returns the user's full name.
     * 
     * @return User's full name.
     */
    String getFullName();
    
    /**
     * Return the user's login name.
     * 
     * @return User's login name.
     */
    String getLoginName();
    
    /**
     * Return the user's password.
     * 
     * @return User's password.
     */
    String getPassword();
    
    /**
     * Returns the user's security domain.
     * 
     * @return User's security domain.
     */
    ISecurityDomain getSecurityDomain();
    
    /**
     * Returns the native user object if this is a proxy.
     * 
     * @return The native user object.
     */
    Object getNativeUser();
}
